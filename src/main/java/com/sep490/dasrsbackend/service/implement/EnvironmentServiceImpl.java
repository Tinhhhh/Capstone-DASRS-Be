package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Environment;
import com.sep490.dasrsbackend.model.entity.ScoredMethod;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewEnvironment;
import com.sep490.dasrsbackend.model.payload.response.EnvironmentResponse;
import com.sep490.dasrsbackend.model.payload.response.ListEnvironment;
import com.sep490.dasrsbackend.model.payload.response.ScoredMethodResponse;
import com.sep490.dasrsbackend.repository.EnvironmentRepository;
import com.sep490.dasrsbackend.service.EnvironmentService;
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
public class EnvironmentServiceImpl implements EnvironmentService {
    private final EnvironmentRepository environmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public void newEnvironment(NewEnvironment request) {
        Environment environment = modelMapper.map(request, Environment.class);
        environment.setStatus(EnvironmentStatus.ACTIVE);
        environmentRepository.save(environment);
    }

    @Override
    public EnvironmentResponse getEnvironment(Long id) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        return modelMapper.map(environment, EnvironmentResponse.class);
    }

    @Override
    public ListEnvironment getAllEnvironment(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Environment> environments = environmentRepository.findAll(pageable);
        List<EnvironmentResponse> environmentResponses = environments.stream()
                .map(environment -> modelMapper.map(environment, EnvironmentResponse.class))
                .toList();

        ListEnvironment listEnvironment = new ListEnvironment();
        listEnvironment.setContent(environmentResponses);
        listEnvironment.setTotalPages(environments.getTotalPages());
        listEnvironment.setTotalElements(environments.getTotalElements());
        listEnvironment.setPageSize(environments.getNumber());
        listEnvironment.setPageNo(environments.getSize());
        listEnvironment.setLast(environments.isLast());

        return listEnvironment;
    }
}
