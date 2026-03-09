package com.be9expensphie.expensphie_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CursorDTO<T> {
    private Long nextCursor;
    boolean hasMore;
    private List<T> data;
}
