package com.esteban.products.error;

import com.esteban.products.model.ItemDto;
import lombok.Getter;
import java.util.List;

public class MissingStockException extends RuntimeException{

    @Getter
    private List<ItemDto> items;

    public MissingStockException(String message, List<ItemDto> items) {
        super(message);
        this.items = items;
    }
}
