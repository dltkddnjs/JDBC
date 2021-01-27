package com.kh.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.kh.model.vo.Member;

// Dao (Data Access Object) : DB에 직접적으로 접근하는 담당 (접근해서 sql문 실행 및 결과 받기)
public class MemberDao {
	
	/*
	 *  * JDBC용 객체
	 *  - Connection : DB의 연결정보를 담고있는 객체
	 *  - [Prepared]Statement : DB에 SQL문 전달해서 실행하고 그 결과를 받아내는 객체
	 *  - ResultSet  : SELECT문 수행 후 조회된 결과들이 담겨있는 객체
	 *  
	 *  * JDBC 처리 순서
	 *  1) jdbc driver 등록 : 해당 DBMS가 제공하는 클래스 등록
	 *  2) Connection 생성   : 연결하고자 하는 DB정보를 입력 해서 DB와 연결하면서 생성
	 *  3) Statement 생성     : Connection 객체를 이용해서 생성 (sql문 실행 및 결과받는 객체)
	 *  4) sql문 전달하면서 실행 : Statement 객체를 이용해서 sql문 실행
	 *  5) 결과 받기 			: 바로 결과 받기
	 *  		> SELECT문 --> ResultSet객체 (조회된 데이터들이 담겨있음)	--> 6_1)
	 *  		>   DML 문 --> int (처리된 행의 갯수)						--> 6_2)
	 *  
	 *  6_1) ResultSet에 담겨있는 데이터들 하나씩 하나씩 뽑아서 vo객체에 주섬주섬담기
	 *  6_2) 트랜잭션 처리 (성공이면 commit, 실패면 rollback)
	 *  
	 *  7) 다 쓴 JDBC용 객체들 반드시 자원 반납 (close)	--> 생성된 역순으로
	 */

	
	/**
	 * 사용자가 입력한 값들로 insert문 실행하는 메소드
	 * @param m	 --> 사용자가 입력한 값들이 잔뜩 담겨잇는 Member 객체
	 */
	public int insertMember(Member m) { // insert문 => 처리된 행 수 => 트랜잭션 처리!
		
		// 필요한 변수들 먼저 셋팅
		
		int result = 0; // 처리된 결과(처리된 행 수)를 받아줄 변수
		
		Connection conn = null; // DB의 연결정보를 담는 객체
		Statement stmt = null;  // SQL문 실행 후 결과를 받는 객체
		
		// 실행할 sql문(완성형태로 만들어둘것!!) --> 끝에 세미콜론 있으면 안됨!!
		String sql = "INSERT INTO MEMBER VALUES(SEQ_USERNO.NEXTVAL, "  
										 + "'" + m.getUserId()   + "', "
										 + "'" + m.getUserPwd()  + "', "
										 + "'" + m.getUserName() + "', "
										 + "'" + m.getGender()   + "', "
										 +       m.getAge()      + ", "
										 + "'" + m.getEmail()    + "', "
										 + "'" + m.getPhone()    + "', "
										 + "'" + m.getAddress()  + "', "
										 + "'" + m.getHobby()    + "', SYSDATE)";
		
		//System.out.println(sql);
		
		try {
			// 1) jdbc driver 등록
			Class.forName("oracle.jdbc.driver.OracleDriver"); // ojdbc6.jar 파일 추가되어있는지, 오타없는지
			
			// 2) Connection 객체 생성 (DB에 연결 --> url, 계정명, 계정비밀번호)
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			
			// 3) Statement 객체 생성
			stmt = conn.createStatement();
			
			// 4, 5) DB에 SQL문 전달 하면서 실행 후 결과 받기 (처리된 행 수)
			result = stmt.executeUpdate(sql);
			
			// 6_2) 트랜잭션 처리
			if(result > 0) { // 성공했을 경우
				conn.commit();
			}else { // 실패했을 경우
				conn.rollback();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			
			try {
				// 7) 다쓴 JDBC용 객체 자원 반납 (close) --> 생성된 역순
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			
		}
		
		return result;
		
	}
	
	public ArrayList<Member> selectList() { // select문 => ResultSet객체 (여러행)
		
		// 필요한 변수들 셋팅
		
		// 처리된 결과(조회된 회원들(여러회원) == 여러행)들을 담아줄 ArrayList생성 (현재 텅빈 리스트)
		ArrayList<Member> list = new ArrayList<>();
		
		Connection conn = null;		// DB 연결정보담는 객체
		Statement stmt = null;		// SQL문 실행 및 결과받는객체
		ResultSet rset = null;		// SELECT문 실행 된 조회 결과값들이 처음에 실질적으로 담길 객체
		
		String sql = "SELECT * FROM MEMBER";
		
		
		try {
			// 1) jdbc driver 등록
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			// 2) Connection 객체 생성
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			
			// 3) Statement 객체 생성
			stmt = conn.createStatement();
			
			// 4, 5) SQL문 전달해서 실행 후 결과 받기 (ResultSet객체)
			rset = stmt.executeQuery(sql); // select문은 executeQuery메소드를 이용
										   // dml문(insert,update,delete)은 executeUpdate메소드를 이용
			
			// 6_1)
			while(rset.next()) { // 행 커서 움직여주는 역할, 뿐만아니라 해당 행이 있으면 true 없으면 false 반환
				
				// 현재 rset의 커서가 가리키고 있는 한 행의 데이터들 싹 다 뽑아서 Member객체 담기
				Member m = new Member();
				// rset으로부터 어떤 컬럼에 해당하는 값을 뽑을 건지 제시해주면됨!! (컬럼명!! 대소문자가리지않음)
				m.setUserNo(rset.getInt("USERNO"));
				m.setUserId(rset.getString("USERID"));
				m.setUserPwd(rset.getString("USERPWD"));
				m.setUserName(rset.getString("USERNAME"));
				m.setGender(rset.getString("GENDER"));
				m.setAge(rset.getInt("AGE"));
				m.setEmail(rset.getString("EMAIL"));
				m.setPhone(rset.getString("PHONE"));
				m.setAddress(rset.getString("ADDRESS"));
				m.setHobby(rset.getString("HOBBY"));
				m.setEnrollDate(rset.getDate("ENROLLDATE"));
				// 한 행에 대해서 한 Member객체에 담는거 끝!
				
				list.add(m); // 리스트에 해당 회원 객체 차곡차곡 담기
			}
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			
			try {
				// 7) 다쓴 자원 반납 --> 생성된 역순으로
				rset.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return list;	
	}
	
	
	public Member selectByUserId(String userId) { // select문 => ResultSet객체 (한 행)
		
		// 필요한 변수들 셋팅 
		
		// 처리결과(조회된 한 회원)
		Member m = null;
		
		Connection conn = null;	// DB의 연결정보 담는 객체
		Statement stmt = null;  // SQL문 실행 및 결과받는 객체
		ResultSet rset = null;  // 조회결과가 처음에 실질적으로 담기는 객체
		
		String sql = "SELECT * FROM MEMBER WHERE USERID = '" + userId + "'";
		
		
		try {
			// 1) 
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			// 2) 
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			
			// 3)
			stmt = conn.createStatement();
			
			// 4, 5)
			rset = stmt.executeQuery(sql);
			
			// 6_1)
			if(rset.next()) {
				
				m = new Member(rset.getInt("USERNO"),
							   rset.getString("userId"),
							   rset.getString("USERPWD"),
							   rset.getString("username"),
							   rset.getString("GENDER"),
							   rset.getInt("AGE"),
							   rset.getString("email"),
							   rset.getString("PHONE"),
							   rset.getString("ADDRESS"),
							   rset.getString("HOBBY"),
							   rset.getDate("enrollDate"));
				
			}
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 7) 
				rset.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return m; // 조회결과가 없을 경우 return null;
		
		
	}
	
	
	public ArrayList<Member> selectByUserName(String keyword) { // select문 --> ResultSet객체 (여러행)
		// 필요한 변수들 셋팅
		
		// 처리결과 (조회된 회원들(여러행))
		ArrayList<Member> list = new ArrayList<>();  // 텅빈리스트
		
		Connection conn = null; 
		Statement stmt = null;
		ResultSet rset = null;
		
		String sql = "SELECT * FROM MEMBER WHERE USERNAME LIKE '%" + keyword + "%'";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			stmt = conn.createStatement();
			
			rset = stmt.executeQuery(sql);
			
			while(rset.next()) {
				
				// 한 행에 대한 데이터를 --> 한 Member객체에 담기
				// 그 Member객체를 list에 담기
				list.add(new Member(rset.getInt("USERNO"),
									rset.getString("USERID"),
									rset.getString("USERPWD"),
									rset.getString("USERNAME"),
									rset.getString("GENDER"),
									rset.getInt("AGE"),
									rset.getString("EMAIL"),
									rset.getString("PHONE"),
									rset.getString("ADDRESS"),
									rset.getString("HOBBY"),
									rset.getDate("enrollDate")));
				
				
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rset.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return list;
		
	}
	
	
	public int updateMember(Member m) { // update문 => 처리된 행 수(int) => 트랜잭션 처리
		
		// 필요한 변수들 셋팅
		int result = 0;
		
		Connection conn = null;
		Statement stmt = null;
		
		
		String sql = "UPDATE MEMBER "
				      + "SET USERPWD = '" + m.getUserPwd() + "', "
				      +		  "EMAIL = '" + m.getEmail()   + "', "
				      +       "PHONE = '" + m.getPhone()   + "', "
				      +     "ADDRESS = '" + m.getAddress() + "' "
				     + "WHERE USERID = '" + m.getUserId()  + "'";
		
		//System.out.println(sql);
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			stmt = conn.createStatement();
			
			result = stmt.executeUpdate(sql);
			
			if(result > 0) {
				conn.commit();
			}else {
				conn.rollback();
			}
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
		
	}
	
	
	public int deleteMember(String userId) { // delete문 => 처리된 행수(int) => 트랜잭션처리
		
		int result = 0;
		
		Connection conn = null;
		Statement stmt = null;
		
		String sql = "delete from member where userid = '" + userId + "'";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			stmt = conn.createStatement();
			
			result = stmt.executeUpdate(sql);
		
			if(result > 0) {
				conn.commit();
			}else {
				conn.rollback();
			}
		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
