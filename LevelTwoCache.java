import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.io.IOException; 


class Cache {
	int ad_bits; //size of address in bits
	int size; // no. words in cache
	int blk_size; // no. of words in  a block
	int curr_size; //no. of cache lines filled
	String[] tag; //array that stores block addresses
	String[][] block; //array that stores each block and the words in it
	int[] priority; //stores the priority of corresponding block
	int ncl; //no. of cache lines in cache
	int tag_size;//size of block address
	String name; //name of cache
	
	Cache(int size, int blk_size, int ad_bits){ 
		this.size = size;
		this.blk_size = blk_size;
		this.ad_bits = ad_bits;
		ncl = (int)(size/blk_size);
		tag = new String[(int)ncl];
		block = new String[(int)ncl][blk_size];
		priority = new int[(int)ncl];
		curr_size = 0;		
		tag_size = ad_bits - (int)(Math.log(blk_size)/Math.log(2));
	}	
	void print_Cache() {
		System.out.println("_____________________________________________________");
		System.out.println("                     Cache " + name);
		System.out.println("Priority - Tag   ----   Block");
		for(int i = 0; i<ncl; i++) {
		System.out.print(priority[i] + "    -    " + tag[i] + " -- " );
		for(int j = 0; j<blk_size; j++)
			if(j<blk_size-1) 
				System.out.print(block[i][j] + " ");
			else System.out.println(block[i][j]);
		}
		System.out.println("_____________________________________________________");
	}
}

public class LevelTwoCache{
	public static boolean found = false; //helper variable
	public static String word = null;	//helper variable
	public static void main(String args[]) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter cache size(words), block size(words) and address size(bits) :");
		String[] size = reader.readLine().split(" ");
		int S = Integer.parseInt(size[0]); // size of cache in words
		int B = Integer.parseInt(size[1]); // size of a block in words
		int N = Integer.parseInt(size[2]); //size of main memory
		System.out.println("Enter the type of mapping (0 for direct, 1 for fully associative, and 2 n for n way set associative mapping)");
		String[] mapping_info = reader.readLine().split(" ");
		int type = Integer.parseInt(mapping_info[0]);
		int n = 0;
		if(type==2)
			n = Integer.parseInt(mapping_info[1]);
		Cache L1 = new Cache(S/2, B, N); //initialize level 1 cache
		L1.name = "L1";
		Cache L2 = new Cache(S, B, N); //initialize level 2 cache
		L2.name = "L2";
		boolean more_input = true;
		do {
			System.out.println("Enter query (address for read and address data for write)");	
			String[] query = reader.readLine().split(" ");
			String address = query[0];
			String data = null;
			int q_type = query.length;
			if(q_type==2)
				data = query[1];			
			if(type == 0){ //direct
				if(q_type == 1) { //read
					direct_read(L1, address);	//read in L1					
					if(found==false) { //if miss in L1
						System.out.println("ADDRESS " + address + " NOT FOUND IN L1");
						associative_read(L2, address);
					}
					if(found == true) { //if found in either of the caches
						direct_write(L1, address, word);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);
					}
					else System.out.println("ADDRESS " + address + " NOT FOUND IN L2"); //miss
					found = false; //reset the helper variables
					word = null;
				}	
				else { //write
					if(isPresent(address.substring(0, L2.tag_size), L2.tag) == -1) { //block not present in cache
						direct_write(L1, address, data);
						direct_write(L2, address, data);
					}
					else if(isPresent(address.substring(0, L1.tag_size), L1.tag) == -1) { //block not present in L1 but present in L2
						direct_write(L2, address, data);
						direct_write(L1, address, data);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);

					}
					else { //block present in both levels
						direct_write(L1, address, data);
						direct_write(L2, address, data);
					}
				}
			}
			else if(type==1){ //associative
				if(q_type == 1) { //read
					associative_read(L1, address);
					if(found == false) { //if miss in L1
						System.out.println("ADDRESS " + address + " NOT FOUND IN L1 ");
						associative_read(L2, address);
					}
					if(found == true) { //if hit in either of the two levels
						associative_write(L1, address, word);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);
					}
					else System.out.println("ADDRESS " + address + " NOT FOUND IN L2 "); //miss
					found = false; //reset helper variables
					word = null;
				}
				else { //write
					if(isPresent(address.substring(0, L2.tag_size), L2.tag) == -1) { //block not present in cache
						associative_write(L1, address, data);
						associative_write(L2, address, data);
					}
					else if(isPresent(address.substring(0, L1.tag_size), L1.tag) == -1) {	//block not present in L1 but present in L2
						associative_write(L2,address, data);
						associative_write(L1, address, data);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);
					}
					else { //block present in both levels
						associative_write(L1, address, data);
						associative_write(L2, address, data);
					}
				}
			}
			else if(type==2){ //n way set associative
				
				if(q_type == 1) { //read
					n_wayRead(L1, n, address);
					if(found == false) { //miss in L1
						System.out.println("ADDRESS " + address + " NOT FOUND in L1 ");
						n_wayRead(L2, n, address);
					}
					if(found == true) { //address found in either of the level
						n_wayWrite(L1, n, address, word);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);
					}
					else System.out.println("ADDRESS " + address + " NOT FOUND IN L2 "); //miss
					found = false; //reset helper variables
					word = null;
				}
				else { //write
					if(isPresent(address.substring(0, L2.tag_size), L2.tag) == -1) { //block not present in cache
						n_wayWrite(L1, n, address, data);
						n_wayWrite(L2, n, address, data);
					}
					else if(isPresent(address.substring(0, L1.tag_size), L1.tag) == -1) {	//block not present in L1 but present in L2
						n_wayWrite(L2, n, address, data);						
						n_wayWrite(L1, n, address, data);
						L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)] = new String[B];
						System.arraycopy(L2.block[isPresent(address.substring(0, L2.tag_size), L2.tag)], 0, L1.block[isPresent(address.substring(0, L1.tag_size), L1.tag)], 0, B);
					}
					else { //block present in both levels
						n_wayWrite(L1, n, address, data);
						n_wayWrite(L2, n, address, data);
					}
				}
			}		
			L1.print_Cache(); //print caches
			L2.print_Cache();
		}while(more_input);			
	}
	
	public static int isPresent(String s, String[] arr) { //helper function to check the presence of a tag in tag array
		for(int i = 0; i<arr.length; i++) {
			if(arr[i] != null && arr[i].equals(s))
				return i;
		}
		return -1;
	}

	
	public static int min_priority(int[] arr) { //returns the index that has least priority in priority array
		int min = arr[0];
		for(int i = 1; i<arr.length; i++) {
			if(arr[i]<min)
				min = arr[i];
		}
		int idx = -1;
		for(int i = 0; i<arr.length; i++) {
			if(arr[i]==min)
				idx = i;		
		}
		return idx;
	}

	public static void direct_read(Cache L, String adrs) { //read data at a given address using direct mapping
		int index_len = (int)(Math.log(L.ncl)/Math.log(2));
		String offsetB = adrs.substring(L.tag_size);
		String blk_tag = adrs.substring(0,L.tag_size);
		String idxB = adrs.substring(L.tag_size-index_len, L.tag_size);
		int index = Integer.parseInt(idxB, 2);
		int offset = Integer.parseInt(offsetB, 2);
		if(L.tag[index] != null && blk_tag.equals(L.tag[index].substring(0, L.tag_size))) {
			System.out.println(L.block[index][offset]);
			word = L.block[index][offset];
			if(L.priority[index] != L.ncl) {
			for(int i = 0; i<L.tag.length; i++) {
				if(L.tag[i] != null)
					L.priority[i]--;
			}
			L.priority[index] = L.ncl;
			}
			found = true;
		}
	}
	
	public static void direct_write(Cache L, String adrs, String d) { //write data at a given address using direct mapping
		int index_len = (int)(Math.log(L.ncl)/Math.log(2));
		String blk_tag = adrs.substring(0,L.tag_size);
		String idxB = adrs.substring(L.tag_size-index_len, L.tag_size);
		String offsetB = adrs.substring(L.tag_size);
		int index = Integer.parseInt(idxB, 2);
		int offset = Integer.parseInt(offsetB, 2);
		if(L.tag[index]==null) { //add the block in the cache
			L.tag[index] = blk_tag;
			L.block[index][offset] = d;
			L.curr_size++;
			for(int i = 0; i<L.tag.length; i++) {
				if(L.tag[i] != null)
					L.priority[i]--;
			}
			L.priority[index] = L.ncl;
		}
		else if(L.tag[index] != null && blk_tag.equals(L.tag[index].substring(0, L.tag_size))) {
			L.block[index][offset] = d;
			if(L.priority[index]!=L.ncl) {
				for(int i = 0; i<L.tag.length; i++) {
					if(L.tag[i] != null)
						L.priority[i]--;
				}
				L.priority[index] = L.ncl;
			}
		}
		else { //replace with the block at the index in the cache
			System.out.println("BLOCK with tag " + L.tag[index] + " replaced" );
			L.tag[index] = blk_tag;
			L.block[index] = new String[L.block[index].length];
			L.block[index][offset] = d;	
			if(L.priority[index]!=L.ncl) {
				for(int i = 0; i<L.tag.length; i++) {
					if(L.tag[i] != null)
						L.priority[i]--;
				}
				L.priority[index] = L.ncl;
			}
		}		
	}
	
	public static void associative_read(Cache L, String adrs) { //read data at a given address using associative mapping
		String blk_tag = adrs.substring(0,L.tag_size);
		String offsetB = adrs.substring(L.tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		String d = null;
		boolean change = false;
		for(int i = 0; i<L.ncl; i++) {
			if(L.tag[i] != null && blk_tag.equals(L.tag[i].substring(0, L.tag_size))) {
				d = L.block[i][offset];
				change = true;
				if(L.priority[i]!=L.ncl) {
				for(int j = 0; j<L.tag.length; j++) {
					if(L.tag[j] != null)
						L.priority[j]--;
				}
				L.priority[i] = L.ncl;
				}
				break;
			}
		}
		if(change) {
			System.out.println(d);
			word = d;
			found = true;
		}
	}
	
	public static void associative_write(Cache L, String adrs, String d) { //write data at a given address using associative mapping
		String blk_tag = adrs.substring(0,(int)(L.tag_size));
		String offsetB = adrs.substring(L.tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		boolean change = false;
		for(int i = 0; i<L.ncl; i++) {
			if(L.tag[i] != null && blk_tag.equals(L.tag[i].substring(0, (int)(L.tag_size)))) {
				L.block[i][offset] = d;
				change = true;
				if(L.priority[i] != L.ncl) {
				for(int j = 0; j<L.tag.length; j++) {
					if(L.tag[i] != null)
						L.priority[j]--;
				}
				L.priority[i] = L.ncl;
				}
				break;
			}							
		}
		if(!change && L.curr_size<L.ncl) {
			L.tag[L.curr_size] = blk_tag;
			L.block[L.curr_size][offset] = d;
			for(int i = 0; i<L.tag.length; i++) {
				if(L.tag[i] != null)
					L.priority[i]--;
			}
			L.priority[L.curr_size] = L.ncl;
			L.curr_size++;			
		}
		else if(!change && L.curr_size == L.ncl) {
			int idx = min_priority(L.priority);
			System.out.println("BLOCK with tag " + L.tag[idx] + " replaced" );
			L.tag[idx] = blk_tag;
			L.block[idx] = new String[L.blk_size];
			L.block[idx][offset] = d;
			if(L.priority[idx]!=L.ncl) {
			for(int i = 0; i<L.tag.length; i++) {
				if(L.tag[i] != null)
					L.priority[i]--;
			}
			L.priority[idx] = L.ncl;
			}
		}
	}
	
	public static void n_wayRead(Cache L, int n, String adrs) { //read data at a given address using n way set associative mapping
		int nbit_idx = (int)(Math.log10(L.ncl/n)/Math.log10(2));
		String blk_tag = adrs.substring(0,L.tag_size);
		String idxB = adrs.substring(L.tag_size-nbit_idx, L.tag_size);
		int index = Integer.parseInt(idxB, 2);
		String offsetB = adrs.substring(L.tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		boolean change = false;
		String d = null;
		for(int i = n*index; i<n*index + n; i++) {
			if(L.tag[i] != null && blk_tag.equals(L.tag[i].substring(0, L.tag_size))) {
				d = L.block[i][offset];
				change = true;
				if(L.priority[i]!=L.ncl) {
					for(int j = 0; j<L.tag.length; j++) {
						if(L.tag[j] != null)
							L.priority[j]--;
					}
					L.priority[i] = L.ncl;
				}
				break;
			}
		}
		if(change) {
			System.out.println(d);
			word = d;
			found = true;
		}
	}
	
	public static void n_wayWrite(Cache L, int n, String adrs, String d) { //write data at a given address using n way set associative mapping
		int nbit_idx = (int)(Math.log10(L.ncl/n)/Math.log10(2));
		String blk_tag = adrs.substring(0,L.tag_size);
		String idxB = adrs.substring(L.tag_size-nbit_idx, L.tag_size);
		int index = Integer.parseInt(idxB, 2);
		String offsetB = adrs.substring(L.tag_size);
		int offset = Integer.parseInt(offsetB, 2);                
		boolean change = false;
		for(int i = n*index; i<n*index + n; i++) {
			if(L.tag[i] != null && blk_tag.equals(L.tag[i].substring(0, L.tag_size))) {
				L.block[i][offset] = d;
				change = true;
				if(L.priority[i]!=L.ncl) {
				for(int j = 0; j<L.tag.length; j++) {
					if(L.tag[j] != null)
						L.priority[j]--;
				}
				L.priority[i] = L.ncl;
				}
				break;
			}
		}
		if(!change) {
			int idx = -1;
			for(int i = n*index; i<n*index + n; i++) {
				if(L.tag[i] == null) {
					L.tag[i] = blk_tag;
					L.block[i][offset] = d;
					for(int j = 0; j<L.tag.length; j++) {
						if(L.tag[j] != null)
							L.priority[j]--;
					}
					L.priority[i] = L.ncl;
					idx = i;
					break;
				}
			}
			if(idx==-1) {
				int[] subset = Arrays.copyOfRange(L.priority, n*index, n*index + n);
				int min = min_priority(subset);
				System.out.println("BLOCK with tag " + L.tag[n*index + min] + " replaced" );
				L.tag[n*index + min] = blk_tag;
				L.block[n*index + min] = new String[L.blk_size];
				L.block[n*index + min][offset] = d;
				if(L.priority[min + n*index]!=L.ncl) {
				for(int i = 0; i<L.tag.length; i++) {
					if(L.tag[i] != null)
						L.priority[i]--;
				}
				L.priority[min + n*index] = L.ncl;
				}
			}			
		}
	}	
}
