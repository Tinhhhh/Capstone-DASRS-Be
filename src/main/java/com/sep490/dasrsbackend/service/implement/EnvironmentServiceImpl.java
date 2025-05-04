package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Environment;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditEnvironment;
import com.sep490.dasrsbackend.model.payload.request.NewEnvironment;
import com.sep490.dasrsbackend.model.payload.response.EnvironmentResponse;
import com.sep490.dasrsbackend.model.payload.response.ListEnvironment;
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
        listEnvironment.setPageSize(environments.getSize());
        listEnvironment.setPageNo(environments.getNumber());
        listEnvironment.setLast(environments.isLast());

        return listEnvironment;
    }

    @Override
    public void updateEnvironment(Long id, EditEnvironment request) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Environment not found"));

        environment.setName(request.getName().trim());

        if (request.getStatus() != null) {
            environment.setStatus(request.getStatus());
        }

        environmentRepository.save(environment);
    }

    @Override
    public void deleteEnvironment(Long id) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Environment not found"));

        environment.setStatus(EnvironmentStatus.INACTIVE);
        environmentRepository.save(environment);
    }

    @Override
    public ListEnvironment getAllActiveEnvironments(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Environment> environments = environmentRepository.findByStatus(EnvironmentStatus.ACTIVE, pageable);

        List<EnvironmentResponse> environmentResponses = environments.stream()
                .map(env -> modelMapper.map(env, EnvironmentResponse.class))
                .toList();

        ListEnvironment listEnvironment = new ListEnvironment();
        listEnvironment.setContent(environmentResponses);
        listEnvironment.setTotalPages(environments.getTotalPages());
        listEnvironment.setTotalElements(environments.getTotalElements());
        listEnvironment.setPageSize(environments.getSize());
        listEnvironment.setPageNo(environments.getNumber());
        listEnvironment.setLast(environments.isLast());

        return listEnvironment;
    }

    @Override
    public EnvironmentResponse getActiveEnvironment(Long id) {
        Environment environment = environmentRepository.findById(id)
                .filter(env -> env.getStatus() == EnvironmentStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Environment not found or inactive"));

        return modelMapper.map(environment, EnvironmentResponse.class);
    }
}
