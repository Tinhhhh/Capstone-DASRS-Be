package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.MatchType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewMatchType;
import com.sep490.dasrsbackend.model.payload.response.ListMatchType;
import com.sep490.dasrsbackend.model.payload.response.MatchTypeResponse;
import com.sep490.dasrsbackend.repository.MatchTypeRepository;
import com.sep490.dasrsbackend.service.MatchTypeService;
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
public class MatchTypeServiceImpl implements MatchTypeService {
    private final MatchTypeRepository matchTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public void newMatchType(NewMatchType newMatchType) {
        MatchType matchType = MatchType.builder()
                .matchTypeName(newMatchType.getMatchTypeName().trim())
                .matchTypeCode(newMatchType.getMatchTypeCode().toUpperCase())
                .matchDuration(newMatchType.getMatchDuration())
                .finishType(newMatchType.getFinishType())
                .status(MatchTypeStatus.ACTIVE)
                .build();
        matchTypeRepository.save(matchType);
    }

    @Override
    public MatchTypeResponse getMatchType(Long id) {

        MatchType matchType = matchTypeRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));
        return modelMapper.map(matchType, MatchTypeResponse.class);
    }

    @Override
    public ListMatchType getAllMatchType(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<MatchType> matchTypePage = matchTypeRepository.findAll(pageable);

        List<MatchTypeResponse> matchTypeResponses = matchTypePage.map(matchType -> modelMapper.map(matchType, MatchTypeResponse.class)).getContent();

        ListMatchType listMatchType = new ListMatchType();
        listMatchType.setContent(matchTypeResponses);
        listMatchType.setTotalPages(matchTypePage.getTotalPages());
        listMatchType.setTotalElements(matchTypePage.getTotalElements());
        listMatchType.setPageNo(matchTypePage.getNumber());
        listMatchType.setPageSize(matchTypePage.getSize());
        listMatchType.setLast(matchTypePage.isLast());

        return listMatchType;
    }

    @Override
    public void updateMatchType(Long id, NewMatchType newMatchType) {
        MatchType matchType = matchTypeRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Match type not found"));

        matchType.setMatchTypeName(newMatchType.getMatchTypeName().trim());
        matchType.setMatchTypeCode(newMatchType.getMatchTypeCode().toUpperCase());
        matchType.setMatchDuration(newMatchType.getMatchDuration());
        matchType.setFinishType(newMatchType.getFinishType());

        matchTypeRepository.save(matchType);
    }

    @Override
    public void deleteMatchType(Long id) {
        MatchType matchType = matchTypeRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Match type not found"));

        matchType.setStatus(MatchTypeStatus.INACTIVE);
        matchTypeRepository.save(matchType);
    }

}
