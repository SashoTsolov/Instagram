package bg.ittalents.instagram.story;

import bg.ittalents.instagram.util.AbstractController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/stories")
public class StoryController extends AbstractController {

    // GET - localhost:8080/stories/1
    // View story by id
    @GetMapping("/{id}")
    public void getStoryById(@PathVariable("id") long storyId) {
        //TODO: implement method to retrieve the story with the given storyId
    }

    // GET - localhost:8080/stories
    // View all my stories sorted by upload date - DESC
    @GetMapping
    public void getAllStories() {
        //TODO: implement method to retrieve all stories
    }

    // POST - localhost:8080/stories
    // Add story
    @PostMapping
    public void createStory(@RequestBody MultipartFile story) {
        //TODO: implement method to create a new story
    }

    // DELETE - localhost:8080/stories/1
    // Delete story
    @DeleteMapping("/{id}")
    public void deleteStory(@PathVariable("id") long storyId) {
        //TODO: implement method to delete the story with the given storyId
    }
}
