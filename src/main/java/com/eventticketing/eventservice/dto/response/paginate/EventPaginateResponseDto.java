package com.eventticketing.eventservice.dto.response.paginate;

import com.eventticketing.eventservice.dto.response.EventResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventPaginateResponseDto {
    private List<EventResponseDto> dataList;
    private long dataCount;
}
