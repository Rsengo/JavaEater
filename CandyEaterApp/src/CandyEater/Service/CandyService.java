package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CandyService extends CandyServiceBase {
    private static final int ADD_CANDY_THREADS_COUNT  = 1024;

    /**
     * Пул пожирателей.
     */
    private ConcurrentLinkedQueue<ICandyEater> mEatersPool;

    /**
     * Пул конфет для пожирания.
     */
    private Hashtable<Integer, ConcurrentLinkedQueue<ICandy>> mCandies;

    /**
     * Пул потоков.
     */
    private ThreadPoolExecutor mExecutor;

    /**
     * Пул потоков для добавления конфеты.
     */
    private ThreadPoolExecutor mAddCandyExecutor;

    /**
     * Словарь типа "вкус - доступность для пожирания".
     */
    private Hashtable<Integer, Boolean> mEnableFlavoursDict;

    /**
     * Конструктор.
     * @param eaters Список пожирателей.
     */
    public CandyService(ICandyEater[] eaters) {
        super(eaters);
        mEatersPool = new ConcurrentLinkedQueue<>(Arrays.asList(eaters));
        mCandies = new Hashtable<>();
        mExecutor = new ThreadPoolExecutor(
                eaters.length,
                eaters.length,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eaters.length));
        mAddCandyExecutor = new ThreadPoolExecutor(
                1,
                ADD_CANDY_THREADS_COUNT,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(ADD_CANDY_THREADS_COUNT));
        mEnableFlavoursDict = new Hashtable<>();
    }

    /**
     * Добавление конфеты.
     * @param candy Конфета.
     */
    @Override
    public void addCandy(ICandy candy) {
        if (candy == null) {
            throw new NullPointerException("Не передана конфета");
        }

        var thread = new Thread(() -> {
            addCandyFlavour(candy);
            start();
        });
        mAddCandyExecutor.execute(thread);
    }

    /**
     * Запись информации о вкусе.
     * @param candy Конфета.
     */
    private synchronized void addCandyFlavour(ICandy candy) {
        var flavour = candy.getCandyFlavour();

        if (!mEnableFlavoursDict.containsKey(flavour)) {
            mEnableFlavoursDict.put(flavour, true);
            var queue = new ConcurrentLinkedQueue<ICandy>();
            queue.add(candy);
            mCandies.put(flavour, queue);
        } else {
            var queue = mCandies.get(flavour);
            queue.add(candy);
        }
    }

    /**
     * Запуск сервиса.
     */
    private synchronized void start() {
        var flavours = getFreeFlavours();

        if (flavours.isEmpty() || mEatersPool.isEmpty()) {
            return;
        }

        var keysIterator = mCandies.keys().asIterator();
        var keys = new ArrayList<Integer>();
        keysIterator.forEachRemaining(keys::add);

        for (var key : keys) {
            var eater = mEatersPool.peek();

            if (eater == null)
                break;

            var queue = mCandies.get(key);
            var candy = queue.peek();

            if (candy == null)
                continue;

            var flavour = candy.getCandyFlavour();

            var canBeEaten = flavours.stream().anyMatch(x -> x == flavour);

            if (!canBeEaten)
                continue;

            startEater(eater, queue.poll());
        }
    }

    /**
     * Запуск пожирателя.
     * @param eater Пожиратель.
     * @param candy Конфета.
     */
    private void startEater(ICandyEater eater, ICandy candy) {
        var flavour = candy.getCandyFlavour();
        mEnableFlavoursDict.replace(flavour, false);
        var task = new EatTask(
                eater,
                candy,
                () -> {
                    mEnableFlavoursDict.replace(flavour, true);
                    mCandies.remove(candy);
                    start();
                },
                () -> {
                    mEnableFlavoursDict.replace(flavour, true);
                    start();
                });
        mExecutor.execute(task);
    }

    /**
     * Получение вкусов, которые можно обработать.
     * @return Вкусы, доступные для пожирания.
     */
    private  List<Integer> getFreeFlavours() {
        var flavours = mEnableFlavoursDict.keys();
        var spliterator = Spliterators.spliteratorUnknownSize(flavours.asIterator(), 0);
        var filtered = StreamSupport.stream(spliterator, false)
                .filter(mEnableFlavoursDict::get)
                .filter(x -> {
                    var queue = mCandies.get(x);
                    return !queue.isEmpty();
                })
                .collect(Collectors.toList());
        return filtered;
    }
}
