package com.esteban.products.error;

import com.esteban.products.model.ItemDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MissingMessage {

    private String error;

    private List<ItemDto> items;

}
