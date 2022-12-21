//DTO class for creating and updating users
package backbase.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Size;
import java.util.Objects;

public class PatchUserDto {

    @Size(min = 2, max = 50, message = "length should be between 2-50 characters")
    @JsonProperty("first_name")
    private String firstName;

    @Size(min = 2, max = 50, message = "length should be between 2-50 characters")
    @JsonProperty("last_name")
    private String lastName;

    public PatchUserDto() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatchUserDto that = (PatchUserDto) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
