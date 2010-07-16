package fcampos.rawengine3D.loader;

public class TChunk {

	    private int ID;					// The chunk's ID		
		private int length;					// The length of the chunk
		private int bytesRead;					// The amount of bytes read within that chunk
		
		public TChunk()
		{
			setID(0);
			setLength(0);
			setBytesRead(0);
		}
		
		public void setTo(TChunk temp)
		{
			setID(temp.getID());
			setLength(temp.getLength());
			setBytesRead(temp.getBytesRead());
		}

		/**
		 * @param iD the iD to set
		 */
		public void setID(int ID) {
			this.ID = ID;
		}

		/**
		 * @return the iD
		 */
		public int getID() {
			return ID;
		}

		/**
		 * @param length the length to set
		 */
		public void setLength(int length) {
			this.length = length;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @param bytesRead the bytesRead to set
		 */
		public void setBytesRead(int bytesRead) {
			this.bytesRead = bytesRead;
		}
		public void addBytesRead(int bytesRead) {
			this.bytesRead += bytesRead;
		}
		
		
		/**
		 * @return the bytesRead
		 */
		public int getBytesRead() {
			return bytesRead;
		}
	}
	

