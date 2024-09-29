package edu.vt.ece.locks;

public interface TimestampSystem {
    Timestamp[] scan();
    void label(Timestamp timestamp, int i);
}
