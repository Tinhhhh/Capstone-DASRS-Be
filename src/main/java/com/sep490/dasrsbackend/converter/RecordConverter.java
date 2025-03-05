package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Record;
import com.sep490.dasrsbackend.model.payload.request.RecordRequest;
import com.sep490.dasrsbackend.model.payload.response.RecordResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RecordConverter {

    private final ModelMapper modelMapper;

    public RecordConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Record toEntity(RecordRequest request, Match match) {
        Record record = modelMapper.map(request, Record.class);
        record.setMatch(match);
        return record;
    }

    public RecordResponse toResponse(Record entity) {
        return modelMapper.map(entity, RecordResponse.class);
    }
}
