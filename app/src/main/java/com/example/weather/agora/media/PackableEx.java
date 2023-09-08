package com.example.weather.agora.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
