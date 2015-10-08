<!-- doc.py -->
﻿Работа с PostgreSQL
-------------------


SELECT
------

``` SQL

```

INSERT
------
``` SQL

```


UPDATE
------
``` SQL

```


DELETE
------
``` SQL

```


CREATE TABLE
------------
``` SQL

```


Первый поток
Длинный цикл
Пауза: 1 секунда
Ввод строчек
[01_Thread/src/main/java/ASimpleThreads.java](01_Thread/src/main/java/ASimpleThreads.java)

Работа с потоками
-----------------
Остановились
[01_Thread/src/main/java/Main.java](01_Thread/src/main/java/Main.java)

String s = Main.strings.remove();
[01_Thread/src/main/java/Thread2.java](01_Thread/src/main/java/Thread2.java)

t2.wait();
[01_Thread/src/main/java/ThreadDebug.java](01_Thread/src/main/java/ThreadDebug.java)

Лямбда-выражения
counter++;
this
[01_Thread/src/main/java/ThreadDemo.java](01_Thread/src/main/java/ThreadDemo.java)

Первый поток
Второй поток
[01_Thread/src/main/java/ThreadDemo2.java](01_Thread/src/main/java/ThreadDemo2.java)

private static AtomicInteger x = new AtomicInteger(0);
volatile static int x2 = 0;
Запускаем 2 потока
System.out.println("x changed = " + x);
x.incrementAndGet();
Прерывание своего потока
Thread.currentThread().interrupt();
[01_Thread/src/main/java/VolatileDemo.java](01_Thread/src/main/java/VolatileDemo.java)

Имя текущего потока
[01_Thread/src/main/java/thread_samples/T2.java](01_Thread/src/main/java/thread_samples/T2.java)

Создаём много потоков
[01_Thread/src/test/java/VolatileTest.java](01_Thread/src/test/java/VolatileTest.java)

Sleep for a bit so that thread 1 has a chance to start
[01_Thread/src/test/java/vol/ThreadTest.java](01_Thread/src/test/java/vol/ThreadTest.java)

atomic
------
Классы из пакета java.util.concurrent.atomic обеспечивают
выполнение атомарных операций
``` java
public class AtomicDemo {
    static final Object LOCK = new Object();
    static final AtomicInteger ATOMIC_SUM = new AtomicInteger();
    static final CountDownLatch CDL = new CountDownLatch(100000);
    static int sum = 0;
    static volatile int globalI;
    static int threadCount = 0;

    static {
        AtomicInteger atomicInteger = new AtomicInteger(2);
        AtomicLong atomicLong = new AtomicLong(3232L);
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        // Нет: AtomicByte, AtomicShort, AtomicFloat, AtomicDouble, AtomicString
        AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(new int[]{1, 3, 4});
        atomicInteger.addAndGet(10);
        atomicBoolean.set(true);
        atomicBoolean.get();
        atomicLong.addAndGet(100000);
        atomicIntegerArray.addAndGet(1, 10);
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            Thread thread = new MyThread();
            thread.start();
        }
        System.out.println("sum = " + sum);
        System.out.println("ATOMIC_SUM = " + ATOMIC_SUM.get());
        System.out.println("globalI = " + globalI);
        System.out.println("wait");
        Thread.sleep(500);
        CDL.await();
        System.out.println("threadCount = " + threadCount);
        System.out.println("sum = " + sum);
        System.out.println("ATOMIC_SUM = " + ATOMIC_SUM.get());
    }

    static void inc() {
        synchronized (LOCK) {
            sum++;
        }
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            threadCount++;
            for (int i = 0; i < 100; ++i) {
                globalI++;
                inc();
                ATOMIC_SUM.incrementAndGet();
                CDL.countDown();
            }
        }
    }
}
```

[02_Atomic/src/main/java/AtomicDemo.java](02_Atomic/src/main/java/AtomicDemo.java)

1. Загрузить из памяти
2. Поменять значение
3. Записать в память
Подождём оба потока
Какое же значение переменной?
[02_Atomic/src/main/java/SyncThread.java](02_Atomic/src/main/java/SyncThread.java)

1. Загрузить из памяти
2. Поменять значение
3. Записать в память
Подождём оба потока
Какое же значение переменной?
[02_Atomic/src/main/java/SyncThread2.java](02_Atomic/src/main/java/SyncThread2.java)

1. Загрузить из памяти
2. Поменять значение
3. Записать в память
Подождём оба потока
Какое же значение переменной?
[02_Atomic/src/main/java/SyncThread3.java](02_Atomic/src/main/java/SyncThread3.java)

1. Загрузить из памяти
2. Поменять значение
3. Записать в память
Подождём оба потока
Какое же значение переменной?
[02_Atomic/src/main/java/SyncThread4.java](02_Atomic/src/main/java/SyncThread4.java)

1. Загрузить из памяти
2. Поменять значение
3. Записать в память
Подождём оба потока
Какое же значение переменной?
[02_Atomic/src/main/java/SyncThread5.java](02_Atomic/src/main/java/SyncThread5.java)

Страртуем 10000 потоков на increment
Поток пусть поспит случайное время
Подождём теперь 10 секунд
Страртуем 10000 потоков на increment
Подождём теперь 10 секунд
[02_Atomic/src/test/java/SyncronizedTest.java](02_Atomic/src/test/java/SyncronizedTest.java)

Получение несуществующего свойства
[03_Properties/src/test/java/PropertiesTest.java](03_Properties/src/test/java/PropertiesTest.java)

myClass.VALUE2 = 30;
x.VALUE2 = 10;
myClass = new MyClass();
[04_Final/src/java/FinalDemo.java](04_Final/src/java/FinalDemo.java)

Thread.sleep(500);
[webapp/src/main/java/Main.java](webapp/src/main/java/Main.java)

Загружаем драйвер для PostgreSQL
"jdbc:postgresql://ec2-54-247-99-244.eu-west-1.compute.amazonaws.com:5432/dap8baaauorm64?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
"elfkolfqypggvo", "vmnWGnCVY5jbSD5nrpnYdU-FEd");
[webapp/src/main/java/webapp/sql/DirectConnection.java](webapp/src/main/java/webapp/sql/DirectConnection.java)

return all not null elements
[webapp/src/main/java/webapp/storage/ArrayStorage.java](webapp/src/main/java/webapp/storage/ArrayStorage.java)

Strategy
[webapp/src/main/java/webapp/storage/SqlStorage.java](webapp/src/main/java/webapp/storage/SqlStorage.java)

marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
[webapp/src/main/java/webapp/util/JaxbParser.java](webapp/src/main/java/webapp/util/JaxbParser.java)

Действие по-умолчанию
add first item in every edited item
[webapp/src/main/java/webapp/web/ResumeServlet.java](webapp/src/main/java/webapp/web/ResumeServlet.java)

Execute before every test
Tests order is random
[webapp/src/test/java/webapp/storage/StorageTest.java](webapp/src/test/java/webapp/storage/StorageTest.java)

