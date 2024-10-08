package dompoo.jdbc.repository;

import dompoo.jdbc.domain.Member;
import dompoo.jdbc.repository.exception.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTransalator
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {
	
	private final DataSource dataSource;
	private final SQLExceptionTranslator exTranslator;
	
	public MemberRepositoryV4_2(DataSource dataSource) {
		this.dataSource = dataSource;
		this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
	}
	
	@Override
	public Member save(Member member) {
		String sql = "insert into member(member_id, money) values (?,?)";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, member.getMemberId());
			pstmt.setInt(2, member.getMoney());
			pstmt.executeUpdate();
			return member;
		} catch (SQLException e) {
			throw exTranslator.translate("save", sql, e);
		} finally {
			close(con, pstmt, null);
		}
	}
	
	@Override
	public Member findById(String memberId) {
		String sql = "select * from member where member_id = ?";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, memberId);
			
			rs = pstmt.executeQuery();
			if (rs.next()) {
				Member member = new Member();
				member.setMemberId(rs.getString("member_id"));
				member.setMoney(rs.getInt("money"));
				return member;
			} else {
				throw new NoSuchElementException();
			}
		} catch (SQLException e) {
			throw exTranslator.translate("findById", sql, e);
		} finally {
			close(con, pstmt, rs);
		}
	}
	
	@Override
	public int update(String memberId, int money) {
		String sql = "update member set money = ? where member_id = ?";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, money);
			pstmt.setString(2, memberId);
			
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw exTranslator.translate("update", sql, e);
		} finally {
			close(con, pstmt, null);
		}
	}
	
	@Override
	public int delete(String memberId) {
		String sql = "delete from member where member_id = ?";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, memberId);
			
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw exTranslator.translate("delete", sql, e);
		} finally {
			close(con, pstmt, null);
		}
	}
	
	@Override
	public int deleteAll() {
		String sql = "delete from member";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw exTranslator.translate("deleteAll", sql, e);
		} finally {
			close(con, pstmt, null);
		}
	}
	
	private void close(Connection con, Statement stmt, ResultSet rs) {
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(stmt);
		DataSourceUtils.releaseConnection(con, dataSource);
	}
	
	private Connection getConnection() throws SQLException {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		log.info("connection = {}", connection);
		return connection;
	}
	
}
