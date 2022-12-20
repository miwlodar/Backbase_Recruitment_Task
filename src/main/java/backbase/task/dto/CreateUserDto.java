//DTO class for creating and updating users
package backbase.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CreateUserDto {

    @NotEmpty(message = "is required")
    @Size(max = 45, message = "is too long")
    @JsonProperty("first_name")
    private String firstName;

    @NotEmpty(message = "is required")
    @Size(max = 45, message = "is too long")
    @JsonProperty("last_name")
    private String lastName;

    public CreateUserDto() {
    }

    public CreateUserDto(String first_name, String last_name) {
        this.firstName = first_name;
        this.lastName = last_name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "CreateUserDto{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
