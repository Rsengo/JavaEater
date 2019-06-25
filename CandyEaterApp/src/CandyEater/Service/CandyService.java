package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.*;
import java.util.concurrent.*;

public class CandyService extends CandyServiceBase {
    /**
     * Пул пожирателей.
     */
    private final LinkedBlockingQueue<ICandyEater> mEatersPool;

    /**
     * Пул конфет для пожирания.
     */
    private final LinkedBlockingDeque<ICandy> mCandies;

    /**
     * Пул потоков.
     */
    private final ThreadPoolExecutor mExecutor;

    /**
     * Словарь типа "вкус - доступность для пожирания".
     */
    private final Set<Integer> mProcessedFlavours;

    /**
     * Флаг остановки диспетчера.
     */
    private volatile boolean mShouldTerminate;

    /**
     * Поток диспетчера.
     */
    private final Thread mDispatchHandler;

    /**
     * Конструктор.
     * @param eaters Список пожирателей.
     */
    public CandyService(ICandyEater[] eaters) {
        super(eaters);
        mCandies = new LinkedBlockingDeque<>();
        mProcessedFlavours = ConcurrentHashMap.newKeySet();
        mDispatchHandler = new Thread(null, this::dispatchLoop, "Dispatch loop");

        if (eaters == null || eaters.length == 0)
        {
            mExecutor = null;
            mEatersPool = new LinkedBlockingQueue<>();
            terminate();
            return;
        }

        mEatersPool = new LinkedBlockingQueue<>(Arrays.asList(eaters));
        mExecutor = new ThreadPoolExecutor(
                eaters.length,
                eaters.length,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eaters.length));

        mShouldTerminate = false;
        mDispatchHandler.start();
    }

    /**
     * Остановка диспетчера.
     */
    @SuppressWarnings("WeakerAccess")
    public void terminate() {
        mShouldTerminate = true;

        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }


    /**
     * Добавление конфеты.
     * @param candy Конфета.
     */
    @Override
    public void addCandy(ICandy candy) {
        if (candy == null)
            throw new IllegalArgumentException("Не передана конфета");

        if (mEatersPool.isEmpty())
            throw new IllegalStateException("Нет доступных обработчиков");

        mCandies.add(candy);
    }

    /**
     * Запуск пожирателя.
     * @param eater Пожиратель.
     * @param candy Конфета.
     */
    private void startEater(ICandyEater eater, ICandy candy) {
        var flavour = candy.getCandyFlavour();
        var task = new EatTask(
                eater,
                candy,
                () -> {
                    synchronized (mProcessedFlavours) {
                        mProcessedFlavours.remove(flavour);
                    }
                    mEatersPool.add(eater);
                },
                ex -> {
                    ex.printStackTrace();
                    synchronized (mProcessedFlavours) {
                        mProcessedFlavours.remove(flavour);
                    }
                    mCandies.add(candy);
                    mEatersPool.add(eater);
                });
        mExecutor.execute(task);
    }

    private void dispatchLoop() {
        while (!mShouldTerminate) {
            try {
                dispatch();
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Итерация цикла диспетчера.
     * @throws InterruptedException Исключение при прерывании.
     */
    private void dispatch() throws InterruptedException {
        var skiped = new ArrayList<ICandy>();

        while (!mCandies.isEmpty()) {
            var candy = mCandies.take();
            var flavour = candy.getCandyFlavour();

            synchronized (mProcessedFlavours) {
                if (mProcessedFlavours.contains(flavour)) {
                    skiped.add(candy);
                    continue;
                }

                mProcessedFlavours.add(flavour);
            }

            var eater = mEatersPool.take();

            synchronized (mCandies) {
                skiped.iterator().forEachRemaining(mCandies::addFirst);
            }

            startEater(eater, candy);

            break;
        }
    }
}
