/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {
    
    public static void main(String a[]){
        HostBlackListsValidator listsValidator=new HostBlackListsValidator();
        //List<Integer> blackListOccurrences=listsValidator.checkHost("202.24.34.55",100);
        //List<Integer> blackListOccurrences=listsValidator.checkHost("202.24.34.55",Runtime.getRuntime().availableProcessors());
        //List<Integer> blackListOccurrences=listsValidator.checkHost("202.24.34.55",Runtime.getRuntime().availableProcessors()*2);
        //List<Integer> blackListOccurrences=listsValidator.checkHost("202.24.34.55",50);
        List<Integer> blackListOccurrences=listsValidator.checkHost("202.24.34.55",100);
        System.out.println("The host was found in the following blacklists:"+blackListOccurrences);
    }
}