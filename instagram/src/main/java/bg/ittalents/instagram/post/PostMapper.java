package bg.ittalents.instagram.post;

import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.DTOs.PostWithCommentsDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.post.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    // DTO to entity mappings
    Post createPostDtoToPost(CreatePostDTO createPostDto);
    Post postPreviewDtoToPost(PostPreviewDTO postPreviewDto);
    Post postWithoutCommentsDtoToPost(PostWithoutCommentsDTO postWithoutCommentsDto);
    Post postWithCommentsDtoToPost(PostWithCommentsDTO postWithCommentsDto);

    // Entity to DTO mappings
    CreatePostDTO postToCreatePostDto(Post post);
    PostPreviewDTO postToPostPreviewDto(Post post);
    PostWithoutCommentsDTO postToPostWithoutCommentsDto(Post post);
    PostWithCommentsDTO postToPostWithCommentsDto(Post post);
}
