	package appSpring.restController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import appSpring.component.UserComponent;
import appSpring.model.Action;
import appSpring.model.Fine;
import appSpring.model.User;
import appSpring.service.ActionService;
import appSpring.service.LogicService;
import appSpring.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

	public interface UserDetail extends User.Basic, User.Act, User.Fin, Fine.Basic, Action.Basic {}

	@Autowired
	private UserService userService;
	@Autowired
	private ActionService actionService;
	@Autowired
	private LogicService logicService;
	@Autowired
	private UserComponent userComponent;

	@JsonView(UserDetail.class)
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<User> postUser(@RequestBody User user) {

		int status = logicService.createAnUser(user);
		if (status == 0) {
			return new ResponseEntity<>(user, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@JsonView(UserDetail.class)
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity<List<User>> getUsers() {

		List<User> users = userService.findAll();
		if (users != null) {
			return new ResponseEntity<>(users, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@JsonView(UserDetail.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<User> getUser(@PathVariable int id) {

		User user = userService.findOne(id);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@JsonView(UserDetail.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<User> deleteUser(@PathVariable Integer id) {

		User userSelected = userService.findOne(id);
		if (userSelected != null) {
			List<Action> actions = actionService.findAll();
			for (Action action : actions) {
				if ((action.getDateLoanReturn() == null) && (action.getUser() == userSelected)) {
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				}
			}
			userService.delete(userSelected);
			return new ResponseEntity<>(userSelected, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@JsonView(UserDetail.class)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<User> putUser(@PathVariable Integer id, @RequestBody User userUpdated) {

		User user = userService.findOne(id);
		if ((user != null) && (user.getId() == userUpdated.getId())) {
			if (userComponent.getLoggedUser() == user) {
				userService.save(userUpdated);
				return new ResponseEntity<>(userUpdated, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
