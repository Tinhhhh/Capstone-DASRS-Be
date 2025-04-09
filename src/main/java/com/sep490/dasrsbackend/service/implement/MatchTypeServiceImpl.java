package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.MatchType;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditMatchType;
import com.sep490.dasrsbackend.model.payload.request.NewMatchType;
import com.sep490.dasrsbackend.model.payload.response.ListMatchType;
import com.sep490.dasrsbackend.model.payload.response.MatchTypeResponse;
import com.sep490.dasrsbackend.repository.MatchTypeRepository;
import com.sep490.dasrsbackend.repository.RoundRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchTypeServiceImpl implements MatchTypeService {
    private final MatchTypeRepository matchTypeRepository;
    private final ModelMapper modelMapper;
    private final RoundRepository roundRepository;

    @Override
    public void newMatchType(NewMatchType newMatchType) {

        if (newMatchType.getPlayerNumber() > 5) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Player number must be less than 6");
        }

        String matchTypeCode = newMatchType.getPlayerNumber() + "P" + newMatchType.getTeamNumber() + "T";

        MatchType matchType = MatchType.builder()
                .matchTypeName(newMatchType.getMatchTypeName().trim())
                .matchTypeCode(matchTypeCode)
                .matchDuration(newMatchType.getMatchDuration())
                .finishType(newMatchType.getFinishType())
                .status(MatchTypeStatus.ACTIVE)
                .playerNumber(newMatchType.getPlayerNumber())
                .teamNumber(newMatchType.getTeamNumber())
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
    public void updateMatchType(Long id, EditMatchType newMatchType) {
        MatchType matchType = matchTypeRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Match type not found"));

        Optional<Round> roundOtp = roundRepository.findByMatchTypeId(id);

        if (roundOtp.isPresent()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Can't edit a match type has used in a round");
        }

        String matchTypeCode = newMatchType.getPlayerNumber() + "P" + newMatchType.getTeamNumber() + "T";

        if (matchType.getStatus() == newMatchType.getStatus()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Can't edit a match type has same status");
        }

        matchType.setMatchTypeName(newMatchType.getMatchTypeName().trim());
        matchType.setMatchTypeCode(matchTypeCode);
        matchType.setMatchDuration(newMatchType.getMatchDuration());
        matchType.setFinishType(newMatchType.getFinishType());
        matchType.setPlayerNumber(newMatchType.getPlayerNumber());
        matchType.setTeamNumber(newMatchType.getTeamNumber());
        matchType.setStatus(MatchTypeStatus.ACTIVE);

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
