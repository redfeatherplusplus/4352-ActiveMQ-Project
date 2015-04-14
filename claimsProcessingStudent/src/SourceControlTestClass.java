
public class SourceControlTestClass {
	public SourceControlTestClass() {
		int x = 7;
		int y = 5;
		float p = 0;
		float q = 0;
				
		
		//conflict 1
		p = x + 10;
		
		//conflict 2
<<<<<<< HEAD
		q = x - 4000;
=======
		q = x - 42;
>>>>>>> branch 'master' of https://github.com/redfeatherplusplus/4352-ActiveMQ-Project
		
		//test adding a line of code 
		q++;
		
		//luke test
		p++;
		
		//elise test
		--p;
		++p;
	}
}
