package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.RaceMap;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewMap;
import com.sep490.dasrsbackend.model.payload.response.ListMapResponse;
import com.sep490.dasrsbackend.model.payload.response.MapResponse;
import com.sep490.dasrsbackend.repository.RaceMapRepository;
import com.sep490.dasrsbackend.service.RaceMapService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceMapServiceImpl implements RaceMapService {

    private final RaceMapRepository raceMapRepository;
    private final ModelMapper modelMapper;

    @Override
    public void newMap(NewMap request) {
        raceMapRepository.save(modelMapper.map(request, RaceMap.class));
    }

    @Override
    public void updateMap(Long id, NewMap request) {

        RaceMap map = raceMapRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));

        map = modelMapper.map(request, RaceMap.class);

        raceMapRepository.save(map);
    }

    @Override
    public MapResponse getMap(Long id) {

        RaceMap map = raceMapRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));

        return modelMapper.map(map, MapResponse.class);
    }

    @Override
    public ListMapResponse getAllMap(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<RaceMap> maps = raceMapRepository.findAll(pageable);
        List<MapResponse> mapResponses = maps.stream()
                .map(map -> modelMapper.map(map, MapResponse.class))
                .toList();

        ListMapResponse listMapResponse = new ListMapResponse();
        listMapResponse.setContent(mapResponses);
        listMapResponse.setTotalPages(maps.getTotalPages());
        listMapResponse.setTotalElements(maps.getTotalElements());
        listMapResponse.setPageSize(maps.getSize());
        listMapResponse.setPageNo(maps.getNumber());
        listMapResponse.setLast(maps.isLast());

        return listMapResponse;
    }
}
