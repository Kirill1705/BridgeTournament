package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.infrastructure.jpa.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    UserDto toDto(UserEntity entity);

    UserEntity toJpa(UserDto dto);
}
