public class FizzBuzz1{
  public static void main(String... arg){
		String fb = "FizzBuzz!!";
		String f = "Fizz";
		String b = "Buzz";
		for(int i=1;i<51;i++){	
			if(i%15 == 0){
				System.out.println(i+fb);
			} else if(i%3 == 0){
				System.out.println(i+f);
			} else if(i%5 == 0){
				System.out.println(i+b);
			} else {
				System.out.println(i);
			}
		}
	}
}
