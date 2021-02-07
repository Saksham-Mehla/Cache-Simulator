import java.util.*;
import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 

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
		System.out.println("                     Cache");
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
	
	int min_priority(int[] arr) { //returns the index that has least priority in priority array
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
	
	void direct_read(String adrs) { //function to read data at given address using direct mapping
		int index_len = (int)(Math.log(ncl)/Math.log(2));
		String offsetB = adrs.substring(tag_size);
		String blk_tag = adrs.substring(0,tag_size);
		String idxB = adrs.substring(tag_size-index_len, tag_size);
		int index = Integer.parseInt(idxB, 2);
		int offset = Integer.parseInt(offsetB, 2);
		if(tag[index] != null && blk_tag.equals(tag[index].substring(0, tag_size))) { //hit
			System.out.println(block[index][offset]);	
			if(priority[index] != ncl) {
			for(int i = 0; i<tag.length; i++) {
				if(tag[i] != null)
					priority[i]--;
			}
			priority[index] = ncl;
			}
		}
		else System.out.println("ADDRESS NOT FOUND " + adrs); //miss
	}
	
	void direct_write(String adrs, String d) {//function to write data at given address using direct mapping
		int index_len = (int)(Math.log(ncl)/Math.log(2));
		String blk_tag = adrs.substring(0,tag_size);
		String idxB = adrs.substring(tag_size-index_len, tag_size);
		String offsetB = adrs.substring(tag_size);
		int index = Integer.parseInt(idxB, 2);
		int offset = Integer.parseInt(offsetB, 2);
		if(tag[index]==null) { //add the block in the cache
			tag[index] = blk_tag;
			block[index][offset] = d;
			curr_size++;
			for(int i = 0; i<tag.length; i++) {
				if(tag[i] != null)
					priority[i]--;
			}
			priority[index] = ncl;
		}
		else if(tag[index] != null && blk_tag.equals(tag[index].substring(0, tag_size))) { //hit
			block[index][offset] = d;
			if(priority[index]!=ncl) {
				for(int i = 0; i<tag.length; i++) {
					if(tag[i] != null)
						priority[i]--;
				}
				priority[index] = ncl;
			}
		}
		else { //replace with the block at the index in the cache
			System.out.println("BLOCK with address " + tag[index] + " replaced" );
			tag[index] = blk_tag;
			block[index] = new String[block[index].length];
			block[index][offset] = d;	
			if(priority[index]!=ncl) {
				for(int i = 0; i<tag.length; i++) {
					if(tag[i] != null)
						priority[i]--;
				}
				priority[index] = ncl;
			}
		}		
	}
	
	void associative_read(String adrs) {//function to read data at given address using associative mapping
		String blk_tag = adrs.substring(0,tag_size);
		String offsetB = adrs.substring(tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		String d = null;
		boolean change = false;
		for(int i = 0; i<ncl; i++) {
			if(tag[i] != null && blk_tag.equals(tag[i].substring(0, tag_size))) { //hit
				d = block[i][offset];
				change = true;
				if(priority[i]!=ncl) {
				for(int j = 0; j<tag.length; j++) {
					if(tag[j] != null)
						priority[j]--;
				}
				priority[i] = ncl;
				}
				break;
			}
		}
		if(change) //found
			System.out.println(d);
		else { //miss
			System.out.println("ADDRESS NOT FOUND " + adrs);
		}
	}
	
	void associative_write(String adrs, String d) {//function to write data at given address using associative mapping
		String blk_tag = adrs.substring(0,(int)(tag_size));
		String offsetB = adrs.substring(tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		boolean change = false;
		for(int i = 0; i<ncl; i++) {
			if(tag[i] != null && blk_tag.equals(tag[i].substring(0, (int)(tag_size)))) { //hit
				block[i][offset] = d;
				change = true;
				if(priority[i] != ncl) {
				for(int j = 0; j<tag.length; j++) {
					if(tag[i] != null)
						priority[j]--;
				}
				priority[i] = ncl;
				}
				break;
			}							
		}
		if(!change && curr_size<ncl) { //insert the block in cache
			tag[curr_size] = blk_tag;
			block[curr_size][offset] = d;
			for(int i = 0; i<tag.length; i++) {
				if(tag[i] != null)
					priority[i]--;
			}
			priority[curr_size] = ncl;
			curr_size++;			
		}
		else if(!change && curr_size == ncl) { //replace a block in the cache with the required block
			int idx = min_priority(priority);
			System.out.println("BLOCK with address " + tag[idx] + " replaced" );
			tag[idx] = blk_tag;
			block[idx] = new String[block[idx].length];
			block[idx][offset] = d;
			if(priority[idx]!=ncl) {
			for(int i = 0; i<tag.length; i++) {
				if(tag[i] != null)
					priority[i]--;
			}
			priority[idx] = ncl;
			}
		}
	}
	
	void n_wayRead(int n, String adrs) { //function to read data at given address using n way set associative mapping
		int nbit_idx = (int)(Math.log10(ncl/n)/Math.log10(2));
		String blk_tag = adrs.substring(0,tag_size);
		String idxB = adrs.substring(tag_size-nbit_idx, tag_size);
		int index = Integer.parseInt(idxB, 2);
		String offsetB = adrs.substring(tag_size);
		int offset = Integer.parseInt(offsetB, 2);
		boolean change = false;
		String d = null;
		for(int i = n*index; i<n*index + n; i++) {
			if(tag[i] != null && blk_tag.equals(tag[i].substring(0, tag_size))) { //hit
				d = block[i][offset];
				change = true;
				if(priority[i]!=ncl) {
					for(int j = 0; j<tag.length; j++) {
						if(tag[j] != null)
							priority[j]--;
					}
					priority[i] = ncl;
				}
				break;
			}
		}
		if(change) //found
			System.out.println(d);
		else { //miss
			System.out.println("ADDRESS NOT FOUND " + adrs);
		}		
	}
	
	void n_wayWrite(int n, String adrs, String d) {//function to write data at given address using set associative mapping
		int nbit_idx = (int)(Math.log10(ncl/n)/Math.log10(2));
		String blk_tag = adrs.substring(0,tag_size);
		String idxB = adrs.substring(tag_size-nbit_idx, tag_size);
		int index = Integer.parseInt(idxB, 2);
		String offsetB = adrs.substring(tag_size);
		int offset = Integer.parseInt(offsetB, 2);                
		boolean change = false;
		for(int i = n*index; i<n*index + n; i++) {
			if(tag[i] != null && blk_tag.equals(tag[i].substring(0, tag_size))) { //hit
				block[i][offset] = d;
				change = true;
				if(priority[i]!=ncl) {
				for(int j = 0; j<tag.length; j++) {
					if(tag[j] != null)
						priority[j]--;
				}
				priority[i] = ncl;
				}
				break;
			}
		}
		if(!change) {
			int idx = -1;
			for(int i = n*index; i<n*index + n; i++) {
				if(tag[i] == null) { //insert block in cache
					tag[i] = blk_tag;
					block[i][offset] = d;
					for(int j = 0; j<tag.length; j++) {
						if(tag[j] != null)
							priority[j]--;
					}
					priority[i] = ncl;
					idx = i;
					break;
				}
			}
			if(idx==-1) { //replace a block in cache with the required block
				int[] subset = Arrays.copyOfRange(priority, n*index, n*index + n);
				int min = min_priority(subset);
				System.out.println("BLOCK with address " + tag[n*index + min] + " replaced" );
				tag[n*index + min] = blk_tag;
				block[n*index + min] = new String[block[n*index + min].length];
				block[n*index + min][offset] = d;
				if(priority[min + n*index]!=ncl) {
				for(int i = 0; i<tag.length; i++) {
					if(tag[i] != null)
						priority[i]--;
				}
				priority[min + n*index] = ncl;
				}
			}			
		}
	}	
}

public class LevelOneCache{
	
	public static void main(String args[]) throws IOException{
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
		if(type == 2)
			n = Integer.parseInt(mapping_info[1]);
		Cache cache = new Cache(S, B, N);
		boolean more_input = true;
		do {
			System.out.println("Enter query (address for read and address data for write)");
			String[] query = reader.readLine().split(" ");
			String address = query[0];
			String data = null;
			int q_type = query.length;
			if(q_type==2)
				data = query[1];
			
			if(type == 0){//direct
				if(q_type == 1) //read
					cache.direct_read(address);
				else //write
					cache.direct_write(address, data);
			}
			else if(type==1){ //associative
				if(q_type == 1) //read
					cache.associative_read(address);
				else //write
					cache.associative_write(address, data);				
			}
			else if(type==2){ //n way set associative
				if(q_type == 1) //read
					cache.n_wayRead(n, address);
				else //write
					cache.n_wayWrite(n, address, data);
			}		
			cache.print_Cache(); //print cache
		}while(more_input);			
	}
}
