/* 
 * MIPA - Middleware Infrastructure for Predicate detection in Asynchronous 
 * environments
 * 
 * Copyright (C) 2009 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the term of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.mipa.test;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author Jianping Yu <jianp.yue@gmail.com>
 */
public class PriorityQueueTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        PriorityQueue<Integer> queue;
        
        queue = new PriorityQueue<Integer>(1,
                                  new Comparator<Integer>() {
            public int compare(Integer i, Integer j) {
                return j - i;
            }
        });

        queue.add(new Integer(1));
        queue.add(new Integer(2));
        queue.add(new Integer(3));
        queue.offer(new Integer(4));
        queue.offer(null);
        System.out.println(queue.size());
        
        int size = queue.size();
        for(int i = 0; i < size; i++)
            System.out.print(queue.poll() + " ");
    }

}
