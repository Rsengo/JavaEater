package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CandyService extends CandyServiceBase {
    /**
     * Пул конфет для пожирания.
     */
    private CopyOnWriteArrayList<ICandy> mCandies;

    /**
     * Пул всех вкусов.
     */
    private int[] mAllFlavours;

    /**
     * Пул потоков.
     */
    private ThreadPoolExecutor mExecutor;

    /**
     * Конструктор.
     * @param eaters Список пожирателей.
     * @param allFlavours Список всеъ вкусов.
     */
    public CandyService(ICandyEater[] eaters, int[] allFlavours) {
        super(eaters);
        mCandies = new CopyOnWriteArrayList<>();
        mAllFlavours = allFlavours;
        mExecutor = new ThreadPoolExecutor(
                eaters.length,
                eaters.length,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eaters.length));
    }

    /**
     * Добавление конфеты.
     * @param candy Конфета.
     */
    @Override
    public void addCandy(ICandy candy) {
        mCandies.add(candy);
        start();
    }

    /**
     * Запуск сервиса.
     */
    private void start() {
        var flavours = getFreeFlavours(mEatersPool, mAllFlavours);
        var eaters = getFreeEaters(mEatersPool);

        if (flavours.isEmpty() || eaters.isEmpty()) {
            return;
        }

        for (var candy : mCandies) {
            var eater = eaters.stream().findFirst();

            if (eater.isEmpty())
                break;

            var flavour = candy.getCandyFlavour();
            var canBeEaten = flavours.stream().anyMatch(x -> x == flavour);

            if (!canBeEaten)
                continue;

            startEater(eater.get(), candy);
        }
    }

    /**
     * Запуск пожирателя.
     * @param eater Пожиратель.
     * @param candy Конфета.
     */
    private void startEater(ICandyEater eater, ICandy candy) {
        var task = new EatTask(eater, candy, this::start, this::start);
        mExecutor.execute(task);
    }

    /**
     * Получение вкусов, которые можно обработать.
     * @param eaters Пожиратели.
     * @param allFlavours Все вкусы.
     * @return Вкусы, доступные для пожирания.
     */
    private static List<Integer> getFreeFlavours(ICandyEater[] eaters, int[] allFlavours) {
        var notFreeFlavours = Arrays.stream(eaters).filter(x -> {
            var hasCandy = x.hasCandy();
            return hasCandy;
        }).map(x -> {
            var candy = x.getCandy();
            return candy.getCandyFlavour();
        });

        var freeFlavours = Arrays.stream(allFlavours)
                .filter(x -> notFreeFlavours.allMatch(y -> y != x))
                .boxed()
                .collect(Collectors.toList());

        return freeFlavours;
    }

    /**
     * Получение свободных пожирателей.
     * @param eatersPool Пул пожирателей.
     * @return Список свободных пожирателей.
     */
    private static List<ICandyEater> getFreeEaters(ICandyEater[] eatersPool) {
        var freeEaters = Arrays.stream(eatersPool)
                .filter(x -> !x.hasCandy())
                .collect(Collectors.toList());

        return freeEaters;
    }
}
