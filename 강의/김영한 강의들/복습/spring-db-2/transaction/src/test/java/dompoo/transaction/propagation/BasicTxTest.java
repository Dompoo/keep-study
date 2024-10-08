package dompoo.transaction.propagation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Slf4j
@SpringBootTest
public class BasicTxTest {
    
    @Autowired
    PlatformTransactionManager txManager;
    
    @TestConfiguration
    static class Config {
        
        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }
    
    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        
        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 종료"); // 커밋되면 커넥션도 돌려주고 트랜잭션이 종료된다.
    }
    
    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        
        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 종료"); // 롤백되면 커넥션도 돌려주고 트랜잭션이 종료된다.
    }
    
    // HikariCP를 사용하면 트랜잭션을 얻기 위해 바로 커넥션을 얻을 수 있는게 아니라,
    // 히카리 프록시 객체를 통해 커넥션을 얻을 수 있는데,
    // 두번의 호출에서 프록시 객체는 서로 다르나, 같은 커넥션을 얻고 있다.
    // HikariProxyConnection@1458383791 wrapping conn0
    // HikariProxyConnection@1924594328 wrapping conn0
    // 결론적으로는 매우 직렬적으로 진행된다.
    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 종료");
        
        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManager.commit(tx2);
        log.info("트랜잭션2 커밋 종료");
    }
    
    // 크게 다르지 않다.
    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 종료");
        
        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백 시작");
        txManager.rollback(tx2);
        log.info("트랜잭션2 롤백 종료");
    }
    /*
    트랜잭션이 내부적으로 다시 호출될 때는 상황이 다르다. 즉, 외부에서 커밋이나 롤백되지 않은 채로 다른 트랜잭션을 호출한 것이다. 해당 로직을 호출한 트랜잭션을 외부
    트랜잭션, 호출당한 트랜잭션을 내부 트랜잭션이라 부른다. 이 두 트랜잭션은 하나의 물리 트랜잭션으로 묶이며, 각각의 트랜잭션은 논리 트랜잭션이 된다. 이때의 규칙은
    모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
   
    실제로 트랜잭션 매니저 안에서의 트랜잭션 생성 과정도 다르게 작동한다.
    외부 트랜잭션에서는 신규 트랜잭션 여부를 확인 후, 신규 트랜잭션이므로
    DataSource를 통해 커넥션을 만들고,
    커넥션의 autoCommit을 꺼서 트랜잭션을 실제로 시작한다.
    하지만 내부 트랜잭션에서는 신규 트랜잭션 여부를 확인 후, 신규 트랜잭션이 아니므로
    기존 트랜잭션에 참여하기만 한다.
   
    기존 트랜잭션에 참여하기만 한 내부 트랜잭션에서는 실제 커밋이 이뤄지지 않는다.
    실제 커밋은 외부 트랜잭션의 커밋시점으로 미뤄진다.
    간단히 말하면, 외부 트랜잭션이 '진짜 트랜잭션'이다.
    
    각 트랜잭션이 자신이 진짜인지 참여인지 확인하는 것은 자신의
    isNewTransactio=true 여부를 확인하여 알 수 있다.
    만약 자신이 새로운 트랜잭션이라는 것은 진짜 트랜잭션이라는 것이고,
    동시에 실제 커밋/롤백을 호출해야 한다는 것이다.
     */
    @Test
    void outerCommitInnerCommit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 isNewTransaction : {}", outer.isNewTransaction());
        
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 isNewTransaction : {}", inner.isNewTransaction());
        log.info("내부 커밋"); // 물리 트랜잭션의 커밋은 외부 트랜잭션의 커밋 시점까지 미뤄진다.
        txManager.commit(inner);
        
        log.info("외부 커밋");
        txManager.commit(outer);
    }
    
    // 외부가 롤백되면 너무 당연하게 전체 롤백된다.
    @Test
    void outerRollbackInnerCommit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 isNewTransaction : {}", outer.isNewTransaction());
        
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 isNewTransaction : {}", inner.isNewTransaction());
        log.info("내부 커밋");
        txManager.commit(inner);
        
        log.info("외부 롤백");
        txManager.rollback(outer);
    }
    
    /*
    내부에서 롤백되면 전체 트랜잭션에 rollbackOnly=true로 표시를 해놓는다.
    내부는 실제 트랜잭션을 가지고 있지 않기 때문에, 이렇게 표시만 해놓는 것이다.
    이때, 외부에서 커밋을 하면 UnexpectedRollbackException예외가 발생한다.
    기대하지 않는 롤백을 알리는 예외인 것이다.
     */
    @Test
    void outerCommitInnerRollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 isNewTransaction : {}", outer.isNewTransaction());
        
        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 isNewTransaction : {}", inner.isNewTransaction());
        log.info("내부 롤백");
        txManager.rollback(inner);
        
        log.info("외부 커밋");
        assertThatThrownBy(() ->
            txManager.commit(outer))
            .isExactlyInstanceOf(UnexpectedRollbackException.class);
    }
    
    /*
    REQUIRES_NEW 로 옵션을 주면,
    내/외부 없이 각각 새로운 물리 트랜잭션이 된다.
    단, 외부 트랜잭션에서 내부 트랜잭션으로 넘어갈 때,
    외부 트랜잭션의 Jdbc 커넥션은 잠시 보류 된다.
     */
    @Test
    void outerCommitInnerRollback_RequiresNew() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 isNewTransaction : {}", outer.isNewTransaction()); //true
        
        log.info("내부 트랜잭션 시작");
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(attribute);
        log.info("내부 isNewTransaction : {}", inner.isNewTransaction()); //true
        log.info("내부 롤백");
        txManager.rollback(inner);
        
        log.info("외부 커밋");
        txManager.commit(outer);
    }
    
    
}
