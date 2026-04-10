package com.be9expensphie.expensphie_backend.repositoryTests;


import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.enums.Method;
import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

//check if query data is true or not
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ExpenseRepositoryTest {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager entityManager;//use to reate obj dtb for testing

    @Test//should never return value
    public void findByIdAndHouseholdId_ShouldReturnExpenses(){
        //arrange: prep data, mock obj
        UserEntity user = createUser("test@example.com");
        Household household = createHousehold("Test House", "CODE123", user);
        HouseholdMember member = createMember(user, household, HouseholdRole.ROLE_ADMIN);

        ExpenseEntity expense = createExpense(BigDecimal.valueOf(50), household, member);

        //act: call method
        Optional<ExpenseEntity> found = expenseRepository.findByIdAndHousehold(expense.getId(), household);

        //assert: check res
        assertThat(found).isPresent();//if return data
        assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));//check val
        assertThat(found.get().getHousehold().getId()).isEqualTo(household.getId());
    }

    @Test
    public void findExpensesByHouseholdWithCursor_ShouldReturnCorrectOrder(){
        //arrange
        UserEntity user = createUser("test");
        Household household = createHousehold("Test House 2", "CODE456", user);
        HouseholdMember member = createMember(user, household, HouseholdRole.ROLE_ADMIN);


        for(int i=1; i<=15; i++){
            ExpenseEntity expense = createExpense(BigDecimal.valueOf(i * 10), household, member);
            expense.setDescription("expense" + i);
            entityManager.merge(expense);//update current obj
        }
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 11, Sort.by("id").descending());

        //act
        List<ExpenseEntity> found = expenseRepository.findNextExpense(Long.MAX_VALUE, household, pageable);


        //assert
        assertThat(found).hasSize(11);
        assertThat(found.getFirst().getDescription()).isEqualTo("expense15");
        assertThat(found.getLast().getDescription()).isEqualTo("expense5");
    }

    @Test
    public void findExpenseByStatus_ShouldFilterCorrectly(){
        //arrange
        UserEntity user = createUser("test");
        Household household = createHousehold("Test House 3", "CODE134", user);
        HouseholdMember member = createMember(user, household, HouseholdRole.ROLE_ADMIN);

        //create approved+pending expense
        for(int i=1; i<=3; i++){
            ExpenseEntity expense = createExpense(BigDecimal.valueOf(i * 10), household, member);
            expense.setStatus(ExpenseStatus.APPROVED);
            entityManager.merge(expense);//update current obj
        }

        for(int i=1; i<=2;i++){
            ExpenseEntity expense=createExpense(BigDecimal.valueOf(i*10),household,member);
            expense.setStatus(ExpenseStatus.PENDING);
            entityManager.merge(expense);
        }
        entityManager.flush();

        Pageable pageable=PageRequest.of(0,20,Sort.by("id").descending());

        //act
        List<ExpenseEntity> found=expenseRepository.findExpenseByStatus(household.getId(),ExpenseStatus.PENDING,Long.MAX_VALUE,pageable);

        //assert
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(e->e.getStatus()==ExpenseStatus.PENDING);
    }

    @Test
    public void findExpenseByStatus_WithCursor_ShouldPaginate(){
        //arrange
        UserEntity user = createUser("test");
        Household household = createHousehold("Test House 4", "CODE1389", user);
        HouseholdMember member = createMember(user, household, HouseholdRole.ROLE_ADMIN);

        for(int i=1;i<=12;i++){
            ExpenseEntity expense=createExpense(BigDecimal.valueOf(i*10),household,member);
            expense.setStatus(ExpenseStatus.PENDING);
            entityManager.merge(expense);
        }
        entityManager.flush();

        Pageable pageable1=PageRequest.of(0,10,Sort.by("id").descending());

        //act
        List<ExpenseEntity> found1=expenseRepository.findExpenseByStatus(household.getId(),ExpenseStatus.PENDING,Long.MAX_VALUE,pageable1);
        Long cursor=found1.getLast().getId();

        List<ExpenseEntity> found2=expenseRepository.findExpenseByStatus(household.getId(),ExpenseStatus.PENDING,cursor,pageable1);

        //assert
        assertThat(found1).hasSize(10);
        assertThat(found2).hasSize(2);
        assertThat(found2).allMatch(e->e.getId()<cursor);
    }

    @Test
    public void findExpenseInRange_ShouldFilterByDate() {
        // arrange
        UserEntity user = createUser("test5@example.com");
        Household household = createHousehold("Test House 5", "CODE202", user);
        HouseholdMember member = createMember(user, household, HouseholdRole.ROLE_ADMIN);

        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate oneWeekAgo = today.minusDays(7);

        // Create expense within range (3 days ago)
        ExpenseEntity inRange = createExpense(BigDecimal.valueOf(100), household, member);
        inRange.setDate(threeDaysAgo);
        inRange.setDescription("inRange");
        entityManager.merge(inRange);

        // Create expense outside range (1 week ago)
        ExpenseEntity outOfRange = createExpense(BigDecimal.valueOf(200), household, member);
        outOfRange.setDate(oneWeekAgo);
        outOfRange.setDescription("outOfRange");
        entityManager.merge(outOfRange);

        entityManager.flush();

        // act: Query for last 5 days
        LocalDate start = today.minusDays(5);
        LocalDate end = today.plusDays(1);
        List<ExpenseEntity> found = expenseRepository.findExpenseInRange(
                household.getId(),
                ExpenseStatus.APPROVED,
                start,
                end
        );

        // assert: Should return only the expense from 3 days ago
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDescription()).isEqualTo("inRange");
    }

    // Helper methods to create test data with all required fields
    private UserEntity createUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword("password123");
        user.setFullName("Test User");
        entityManager.persist(user);
        return user;
    }

    private Household createHousehold(String name, String code, UserEntity createdBy) {
        Household household = new Household();
        household.setName(name);
        household.setCode(code);
        household.setCreatedBy(createdBy);
        entityManager.persist(household);
        return household;
    }

    private HouseholdMember createMember(UserEntity user, Household household, HouseholdRole role) {
        HouseholdMember member = new HouseholdMember();
        member.setUser(user);
        member.setHousehold(household);
        member.setRole(role);
        entityManager.persist(member);
        return member;
    }

    private ExpenseEntity createExpense(BigDecimal amount, Household household, HouseholdMember member) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setAmount(amount);
        expense.setHousehold(household);
        expense.setCreated_by(member);
        expense.setReviewed_by(member);
        expense.setCurrency("USD");
        expense.setDate(LocalDate.now());
        expense.setCategory("Food");
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setMethod(Method.EQUAL);
        expense.setDescription("Test expense");
        entityManager.persist(expense);
        return expense;
    }
}
