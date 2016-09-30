# Bottle

## Overview

Bottle is a **Zookeeper** lib for Java language.

Note: to run the unit test in the project, you should have a zookeeper instance running in your local, which can be accessed with '127.0.0.1:2181'.

## API

### 1. ZKBarrier

like [CyclicBarrier](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CyclicBarrier.html), ZKBarrier allows a set of thread to all wait for each other to reach a common barrier point.

* `ZKBarrier` : a barrier with a group name and thread entry count.
* `ZKBarrierEntry` : generated from a `ZKBarrier`, stands for a waiting group member.
  * `enter()` : enter the group waiting.
  * `leave()` : leave the group waiting.  Entries can resume  execution only after all entries have leaved.

You can find a usage in [here]().

## Developer Guide

* Clone codebase

```shell
git clone git@git.nanchao.org:groot/'Bottle'.git
```

* Generate your IDEA project files, and then start your work

```shell
./gradlew idea
```

* Check your build before your commit

```shell
./gradlew check
```