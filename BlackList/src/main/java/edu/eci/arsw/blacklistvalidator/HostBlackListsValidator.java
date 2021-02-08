/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.threads.HostBlackListThread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    public static int getBlackListAlarmCount() { return BLACK_LIST_ALARM_COUNT; }

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress,int n){

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        LinkedList<Integer> globalBlackListOccurrences = new LinkedList<>();
        LinkedList<HostBlackListThread> threads = new LinkedList<>();

        AtomicInteger cuenta = new AtomicInteger(0);
        AtomicInteger listasRevisadas = new AtomicInteger(0);

        int serversNumber = skds.getRegisteredServersCount()/n;
        int cont = 0;

        for (int i = 0; i < n; i++){
            threads.add(new HostBlackListThread(skds, cont, cont + serversNumber, ipaddress, cuenta, listasRevisadas));
            cont = cont + serversNumber;
        }

        for(HostBlackListThread hilo : threads){
            hilo.start();
        }

        for(HostBlackListThread hilo : threads){
            try {
                hilo.join();
                globalBlackListOccurrences.addAll(hilo.getBlackListOccurrences());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (cuenta.get() >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{listasRevisadas.get(), skds.getRegisteredServersCount()});

        return globalBlackListOccurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}