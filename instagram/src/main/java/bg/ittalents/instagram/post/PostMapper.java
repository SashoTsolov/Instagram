package bg.ittalents.instagram.post;

import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    Post postPreviewDTOToEntity(PostPreviewDTO dto);

    PostPreviewDTO entityToPostPreviewDTO(Post entity);
}
