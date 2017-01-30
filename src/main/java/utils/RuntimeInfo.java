package utils;

/**
 * Created by Anthony on 14/04/2016.
 */
public class RuntimeInfo {
    private long m_maxMemory;
    private long m_allocatedMemory;
    private long m_freeMemory;
    private long m_currentTimeMillis;

    public RuntimeInfo(long maxMemory, long allocatedMemory, long freeMemory, long currentTimeMillis) {
        m_maxMemory = maxMemory;
        m_allocatedMemory = allocatedMemory;
        m_freeMemory = freeMemory;
        m_currentTimeMillis = currentTimeMillis;
    }

    public long get_maxMemory() {
        return m_maxMemory;
    }

    public long get_allocatedMemory() {
        return m_allocatedMemory;
    }

    public long get_freeMemory() {
        return m_freeMemory;
    }

    public long get_currentTimeMillis() {
        return m_currentTimeMillis;
    }
}
