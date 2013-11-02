package com.mixshare.rapid_evolution.ui.widgets.common.image;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QTimerEvent;

public abstract class Worker extends QObject {

    protected abstract void execute();

    public Worker(QObject parent) {
        super(parent);
    }

    public void start() {
    if (m_is_running)
        stop();
    m_is_running = true;
    m_timer_id = startTimer(m_delay);
    }

    public void stop() {
    killTimer(m_timer_id);
    m_is_running = false;
    }

    public void setDelay(int delay) {
    m_delay = delay;
    }

    public int delay() {
    return m_delay;
    }

    @Override
    protected void timerEvent(QTimerEvent e) {
    if (e.timerId() == m_timer_id) {
        execute();
        stop();
    }
    }

    private int m_delay = 250;
    private int m_timer_id;
    private boolean m_is_running;
}
