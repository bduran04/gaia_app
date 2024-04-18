package dsd.cohort.application.user;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dsd.cohort.application.ingredient.IngredientEntity;
import dsd.cohort.application.recipe.RecipeEntity;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public List<UserEntity> test() {
        return userService.getAll();
    }

    @PostMapping("/createuser")
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user){

        try {
            UserEntity newUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(newUser);
        }
        catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't Create User");
        }
    }

    @GetMapping("/finduserbyemail")
    public ResponseEntity<UserEntity> findUserByEmail(@RequestParam String email) {
        try {
            UserEntity user = userService.findUserByEmail(email);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(user);
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
        }
    }

    // add a recipe to a users favorites
    @PostMapping("/addrecipetofavorites")
    public ResponseEntity<String> addRecipe(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.addRecipe(userRequestDTO);

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Recipe added to user favorites");
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not add recipe to user's favorites: " + e.getMessage());
        }
    }

    // delete a recipe from a users favorites
    @DeleteMapping("/removerecipefromfavorites")
    public ResponseEntity<String> deleteRecipe(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.deleteRecipe(userRequestDTO.getEmail(), userRequestDTO.getId());

            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not remove from user's favorites: " + e.getMessage());
        }

    }

    // get a users favorites
    @GetMapping("/getuserfavorites/{email}")
    public ResponseEntity<Set<RecipeEntity>> getUserFavorites(@PathVariable String email) {
        try {
            Set<RecipeEntity> userFavorites = userService.getUserFavorites(email);
            return ResponseEntity.status(HttpStatus.OK).body(userFavorites);

        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get user's favorites.");
        }
    }

    @PostMapping("/additemtogrocerylist")
    public ResponseEntity<String> addItemToGroceryList(@RequestBody UserRequestDTO userRequestDTO) {

        try {
            userService.addGroceryItem(userRequestDTO);

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Ingredient added to user's grocery list");
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not add ingredient to user's grocery list: " + e.getMessage());
        }
    }
    @GetMapping("/getgrocerylist/{email}")
    public ResponseEntity<Set<IngredientEntity>> getGroceryList(@PathVariable String email) {
        try {
            Set<IngredientEntity> userGroceryList = userService.getGroceryList(email);
            return ResponseEntity.status(HttpStatus.OK).body(userGroceryList);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get user's grocery list");
        }
    }

    @DeleteMapping("/removefromgrocerylist")
    public ResponseEntity<String> removeFromGroceryList(UserRequestDTO userRequestDTO) {
        try{
            userService.removeFromGroceryList(userRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body("Item removed from user's grocery list.");
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not remove item " +
                    "from users grocery list.");
        }
    }
}
