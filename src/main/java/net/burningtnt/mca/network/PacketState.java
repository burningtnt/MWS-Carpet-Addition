package net.burningtnt.mca.network;

public enum PacketState {
    C2S, S2C, BOTH;

    public boolean isServerSide() {
        return this == PacketState.C2S || this == PacketState.BOTH;
    }

    public boolean isClientSide() {
        return this == PacketState.S2C || this == PacketState.BOTH;
    }
}
