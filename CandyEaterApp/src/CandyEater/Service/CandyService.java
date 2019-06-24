package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CandyService extends CandyServiceBase {
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
        mEnableFlavoursDict = new Hashtable<>();
    }

    /**
     * Добавление конфеты.
     * @param candy Конфета.
     */
    @Override
    public void addCandy(ICandy candy) {
        addCandyFlavour(candy);
        start();
    }

    private void addCandyFlavour(ICandy candy) {
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
    private void start() {
//        var flavours = getFreeFlavours();
//
//        if (flavours.isEmpty() || mEatersPool.isEmpty()) {
//            return;
//        }
//
//        for (var candy : mCandies) {
//            var eater = mEatersPool.peek();
//
//            if (eater == null)
//                break;
//
//            var flavour = candy.getCandyFlavour();
//            var canBeEaten = flavours.stream().anyMatch(x -> x == flavour);
//
//            if (!canBeEaten)
//                continue;
//
//            startEater(eater, candy);
//        }
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
