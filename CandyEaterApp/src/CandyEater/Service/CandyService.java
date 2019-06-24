package CandyEater.Service;

import CandyEater.Candy.Flavours;
import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Tasks.EatTask;

import java.util.Hashtable;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CandyService extends CandyServiceBase {
    /**
     * Пул пожирателей.
     */
    private CopyOnWriteArrayList<ICandyEater> mEatersPool;

    /**
     * Пул конфет для пожирания.
     */
    private CopyOnWriteArrayList<ICandy> mCandies;

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
        mEatersPool = new CopyOnWriteArrayList<>(eaters);
        mCandies = new CopyOnWriteArrayList<>();
        mExecutor = new ThreadPoolExecutor(
                eaters.length,
                eaters.length,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eaters.length));
        mEnableFlavoursDict = getEnableFlavoursDict();
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
        var flavours = getFreeFlavours(mEnableFlavoursDict);

        if (flavours.isEmpty() || mEatersPool.isEmpty()) {
            return;
        }

        for (var candy : mCandies) {
            var eater = mEatersPool.stream().findFirst();

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
    private static List<Integer> getFreeFlavours(Hashtable<Integer, Boolean> enableFlavoursDict) {
        var flavours = enableFlavoursDict.keys();
        var spliterator = Spliterators.spliteratorUnknownSize(flavours.asIterator(), 0);
        var filtered = StreamSupport.stream(spliterator, false)
                .filter(enableFlavoursDict::get)
                .collect(Collectors.toList());
        return filtered;
    }

    /**
     * Получение словаря доступности вкусов.
     * @return Словарь доступности вкусов.
     */
    private static Hashtable<Integer, Boolean> getEnableFlavoursDict() {
        var flavoursEnum = Flavours.values();
        var hashtable = new Hashtable<Integer, Boolean>();

        for (var flavourEnumItem : flavoursEnum) {
            var flavour = flavourEnumItem.ordinal();
            hashtable.put(flavour, true);
        }

        return hashtable;
    }
}
