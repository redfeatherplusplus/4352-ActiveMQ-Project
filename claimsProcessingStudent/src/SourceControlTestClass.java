
public class SourceControlTestClass {
	public SourceControlTestClass() {
		int x = 7;
		int y = 5;
		float p = 0;
		float q = 0;
				
		
		//conflict 1
		p = x + 1;
		
		//conflict 2
		q = x - 4;
		
		//test adding a line of code 
		q++;
		
		//luke test
		p++;
		
		//elise test
		--p;
		++p;
	}
}
