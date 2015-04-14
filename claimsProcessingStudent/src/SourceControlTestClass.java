
public class SourceControlTestClass {
	public SourceControlTestClass() {
		int x = 7;
		int y = 5;
		float p = 0;
		float q = 0;
				
		
		//conflict 1
		p = x + 10;
		
		//conflict 2
		p = p + 1;
		
		//test adding a line of code 
		q++;
		
		//luke test
		p++;
		
		//elise test
		--p;
		++p;
	}
}
