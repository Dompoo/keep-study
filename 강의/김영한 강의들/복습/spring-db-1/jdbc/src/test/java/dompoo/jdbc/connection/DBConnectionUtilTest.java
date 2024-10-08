package dompoo.jdbc.connection;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionUtilTest {

	@Test
	void connection() {
		Connection connection = DBConnectionUtil.getConnection();
		Assertions.assertThat(connection).isNotNull();
		// 우리의 connection은 org.h2.jdbc.JdbcConnection 이다.
	}
	

}