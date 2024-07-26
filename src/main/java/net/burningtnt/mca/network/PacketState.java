package net.burningtnt.mca.network;

public enum PacketState {
    C2S, S2C, BOTH;

    public boolean isC2S() {
        return this == PacketState.C2S || this == PacketState.BOTH;
    }

    public boolean isS2C() {
        return this == PacketState.S2C || this == PacketState.BOTH;
    }
}
