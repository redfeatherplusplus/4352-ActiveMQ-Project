
public class SourceControlTestClass {
	public SourceControlTestClass() {
		int x = 7;
		int y = 5;
		float p = 0;
		float q = 0;
				
		
		//conflict 1
<<<<<<< HEAD
		p = x + 20;
=======
		p = x + 30;
>>>>>>> branch 'master' of https://github.com/redfeatherplusplus/4352-ActiveMQ-Project
		
		//conflict 2
		p++;
		
		//test adding a line of code 
		q++;
		
		//luke test
		p++;
		
		//elise test
		--p;
		++p;
	}
}
