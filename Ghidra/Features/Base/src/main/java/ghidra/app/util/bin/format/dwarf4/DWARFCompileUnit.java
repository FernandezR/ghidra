/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.util.bin.format.dwarf4;

import java.io.File;
import java.io.IOException;

import ghidra.app.util.bin.format.dwarf4.encoding.*;

/**
 * DWARFCompileUnit hold some values retrieved from a DWARF DW_TAG_compile_unit DIE.
 * <p>
 */
public class DWARFCompileUnit {
	private final String name;
	private final String producer;
	private final String comp_dir;
	private final Number high_pc;
	private final Number low_pc;
	private final Number language;
	private final boolean hasDWO;

	private DWARFLine line = null;

	public static DWARFCompileUnit read(DIEAggregate diea)
			throws IOException, DWARFException {
		if (diea.getTag() != DWARFTag.DW_TAG_compile_unit) {
			throw new IOException("Expecting a DW_TAG_compile_unit DIE, found " + diea.getTag());
		}

		String name = diea.getString(DWARFAttribute.DW_AT_name, null);
		String producer = diea.getString(DWARFAttribute.DW_AT_producer, null);
		String comp_dir = diea.getString(DWARFAttribute.DW_AT_comp_dir, null);

		Number high_pc = null, low_pc = null, language = null;

		if (diea.hasAttribute(DWARFAttribute.DW_AT_low_pc)) {
			low_pc = diea.getLowPC(0);
		}

		// if lowPC and highPC values are the same, don't read the high value
		// because Ghidra can't express an empty range.
		if (diea.hasAttribute(DWARFAttribute.DW_AT_high_pc) && !diea.isLowPCEqualHighPC()) {
			high_pc = diea.getHighPC();
		}

		if (diea.hasAttribute(DWARFAttribute.DW_AT_language)) {
			language = diea.getUnsignedLong(DWARFAttribute.DW_AT_language, -1);
		}

		boolean hasDWO = diea.hasAttribute(DWARFAttribute.DW_AT_GNU_dwo_id) &&
			diea.hasAttribute(DWARFAttribute.DW_AT_GNU_dwo_name);

		DWARFLine line = DWARFLine.read(diea);

		return new DWARFCompileUnit(name, producer, comp_dir, low_pc, high_pc, language, hasDWO,
			line);
	}

	/*
	 * Construct a DWARF compile unit with the given values.
	 */
	public DWARFCompileUnit(String name, String producer, String comp_dir, Number low_pc,
			Number high_pc, Number language, boolean hasDWO, DWARFLine line) {
		this.name = name;
		this.producer = producer;
		this.comp_dir = comp_dir;
		this.low_pc = low_pc;
		this.high_pc = high_pc;
		this.language = language;
		this.hasDWO = hasDWO;
		this.line = line;
	}

	/**
	 * Get the name of the compile unit
	 * @return the name of the compile unit
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the filename of the compile unit
	 * @return the filename of the compile unit
	 */
	public String getFileName() {
		return getName() == null ? null : new File(getName()).getName();
	}

	/**
	 * Get a file name with the full path included based on a file index.
	 * @param index index of the file
	 * @return file name with full path or null if line information does not exist
	 * @throws IllegalArgumentException if a negative or invalid file index is given
	 */
	public String getFullFileByIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative file index was given.");
		}
		if (this.line == null) {
			return null;
		}

		return this.line.getFullFile(index, this.comp_dir);
	}

	/**
	 * Get a file name based on a file index.
	 * @param index index of the file
	 * @return file name or null if line information does not exist
	 * @throws IllegalArgumentException if a negative or invalid file index is given
	 */
	public String getFileByIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative file index was given.");
		}
		if (this.line == null) {
			return null;
		}

		return this.line.getFile(index, this.comp_dir);
	}

	/**
	 * Checks validity of a file index number.
	 * 
	 * @param index file number, 1..N
	 * @return boolean true if index is a valid file number, false otherwise
	 */
	public boolean isValidFileIndex(int index) {
		return line.isValidFileIndex(index);
	}

	/**
	 * Get the producer of the compile unit
	 * @return the producer of the compile unit
	 */
	public String getProducer() {
		return this.producer;
	}

	/**
	 * Get the compile directory of the compile unit
	 * @return the compile directory of the compile unit
	 */
	public String getCompileDirectory() {
		return this.comp_dir;
	}

	/**
	 * Get the high PC value of the compile unit
	 * @return the high PC value of the compile unit
	 */
	public Number getHighPC() {
		return this.high_pc;
	}

	/**
	 * Get the low PC value of the compile unit
	 * @return the low PC value of the compile unit
	 */
	public Number getLowPC() {
		return this.low_pc;
	}

	/**
	 * Get the source language of the compile unit.
	 * <p>
	 * See {@link DWARFSourceLanguage} for values.
	 * 
	 * @return the source language of the compile unit
	 */
	public int getLanguage() {
		return this.language == null ? -1 : this.language.intValue();
	}

	public boolean hasDWO() {
		return hasDWO;
	}

	@Override
	public String toString() {
		return String.format(
			"DWARFCompileUnit [name=%s, producer=%s, comp_dir=%s, high_pc=%s, low_pc=%s, language=%s, hasDWO=%s, line=%s]",
			name, producer, comp_dir, high_pc, low_pc, language, hasDWO, line);
	}

}
