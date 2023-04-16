package bg.ittalents.instagram.story;

import bg.ittalents.instagram.post.entities.Post;
import bg.ittalents.instagram.story.DTOs.CreateStoryDTO;
import bg.ittalents.instagram.story.DTOs.StoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper
public interface StoryMapper {

    StoryMapper INSTANCE = Mappers.getMapper(StoryMapper.class);

    // DTO to entity mappings
    Post createStoryDtoToPost(CreateStoryDTO createStoryDto);
    Post storyDtoToPost(StoryDTO storyDto);

    // Entity to DTO mappings
    CreateStoryDTO postToCreateStoryDto(Post post);
    StoryDTO postToStoryDto(Post post);
}