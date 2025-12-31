package io.voluble.michellelib.cooldown;

final class SystemCooldownClock implements CooldownClock {
    static final SystemCooldownClock INSTANCE = new SystemCooldownClock();

    private SystemCooldownClock() {
    }

    @Override
    public long nowNanoTime() {
        return System.nanoTime();
    }

    @Override
    public long nowEpochMillis() {
        return System.currentTimeMillis();
    }
}



