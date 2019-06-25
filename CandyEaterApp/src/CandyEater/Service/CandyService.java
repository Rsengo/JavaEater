package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CandyService extends CandyServiceBase {
    /**
     * Пул пожирателей.
     */
    private final LinkedBlockingQueue<ICandyEater> eatersPool;

    /**
     * Пул конфет для пожирания.
     */
    private final LinkedBlockingDeque<ICandy> candies;

    /**
     * Пул потоков.
     */
    private final ThreadPoolExecutor executor;

    /**
     * Словарь типа "вкус - доступность для пожирания".
     */
    private final Set<Integer> processedFlavours;

    /**
     * Флаг остановки диспетчера.
     */
    private volatile boolean shouldTerminate;

    /**
     * Поток диспетчера.
     */
    private final Thread dispatchHandler;

    /**
     * Конструктор.
     * @param eaters Список пожирателей.
     */
    public CandyService(ICandyEater[] eaters) {
        super(eaters);

        if (eaters == null) {
            throw new IllegalArgumentException("Не переданы пожиратели");
        }

        var validEaters = Arrays.stream(eaters)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        candies = new LinkedBlockingDeque<>();
        processedFlavours = ConcurrentHashMap.newKeySet();
        dispatchHandler = new Thread(null, this::dispatchLoop, "Dispatch loop");

        if (validEaters.isEmpty())
        {
            executor = null;
            eatersPool = new LinkedBlockingQueue<>();
            terminate();
            return;
        }

        final int eatersCount = validEaters.size();
        eatersPool = new LinkedBlockingQueue<>(validEaters);
        executor = new ThreadPoolExecutor(
                eatersCount,
                eatersCount,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eatersCount));

        shouldTerminate = false;
        dispatchHandler.start();
    }

    /**
     * Остановка диспетчера.
     */
    @SuppressWarnings("WeakerAccess")
    public void terminate() {
        shouldTerminate = true;

        if (executor != null) {
            executor.shutdown();
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

        if (eatersPool.isEmpty())
            throw new IllegalStateException("Нет доступных обработчиков");

        candies.add(candy);
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
                    synchronized (processedFlavours) {
                        processedFlavours.remove(flavour);
                    }
                    eatersPool.add(eater);
                },
                ex -> {
                    ex.printStackTrace();
                    synchronized (processedFlavours) {
                        processedFlavours.remove(flavour);
                    }
                    candies.add(candy);
                    eatersPool.add(eater);
                });
        executor.execute(task);
    }

    private void dispatchLoop() {
        while (!shouldTerminate) {
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

        while (!candies.isEmpty()) {
            var candy = candies.take();
            var flavour = candy.getCandyFlavour();

            synchronized (processedFlavours) {
                if (processedFlavours.contains(flavour)) {
                    skiped.add(candy);
                    continue;
                }

                processedFlavours.add(flavour);
            }

            var eater = eatersPool.take();

            synchronized (candies) {
                skiped.iterator().forEachRemaining(candies::addFirst);
            }

            startEater(eater, candy);

            break;
        }
    }
}
