package cn.spring.dao;

import org.springfremaker.bean.annotation.Service;
import org.springfremaker.bean.util.TransactionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Service
public class UserDao {

    public  void insert(String username,int money){
        Connection root =null;
        try {

            root=TransactionManager.connection();
            PreparedStatement preparedStatement = root.prepareStatement("update users set money=money-" + money + " where username='" + username+"'");
            int i = preparedStatement.executeUpdate();
            System.out.println(i);

        }catch (Exception e){
            e.printStackTrace();

        }

    }

    public static void main(String[] args) {
        new UserDao().insert("zhangsan",500);
    }
}
