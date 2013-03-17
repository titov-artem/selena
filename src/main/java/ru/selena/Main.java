package ru.selena;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Date: 12/15/12
 * Time: 3:53 PM
 *
 * @author Artem Titov
 */
public class Main {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("core-config.xml").start();
    }

}
