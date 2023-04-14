package bg.ittalents.instagram.story;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper
public interface StoryMapper {

    StoryMapper INSTANCE = Mappers.getMapper(StoryMapper.class);


}