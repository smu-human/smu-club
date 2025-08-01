package com.smuclub.smu_club;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;


public class JapTest {

    /*
                -JPA 동작 순서-
        1. EntityManagerFactory 생성 (팩토리 생성)
        2. EntityManager 생성 (스프링으로 치면 컨테이너같은 느낌 -> JPA에선 트랜잭션에 대한 전반적인 관리 해줌)
        3. EntityTransaction 생성 (커넥션이 존재해야 트랜잭션이 실행될 수 있음)
        4. Transaction 시작
        5. 트랜잭션 수행
        6. persist()로 저장
        7. commit()으로 트랜잭션 수행
        8. 역순(transaction -> entityManager -> entityManagerFacotry)으로 연결 끊기
    */

    public static void main(String[] args){


        EntityManagerFactory emf = Persistence.createEntityManagerFactory("testTest"); //persistence.xml 에서 persistenceUnit의 설정에 따라 팩토리가 생성됨.
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try{

            tx.commit();
        }
        catch(Exception e){
            tx.rollback();
        }
        finally{
            em.close();
            emf.close();
        }
    }
}
